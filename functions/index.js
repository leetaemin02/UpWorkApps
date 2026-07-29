/**
 * Firebase Cloud Functions for JobSearchApp (UpWorkApps-main1)
 * Optimized Groq AI Integration (llama-3.3-70b-versatile) for:
 * 1. Task 1: Job Recommendation by candidate skills (recommendJobsOnSkillUpdate)
 * 2. Task 2: Applicant Ranking (CV vs Job Description) (rankApplicantOnApplication)
 * 3. Task 3: CV PDF Parsing to JSON (parseCvOnUpload & parseCvPdf)
 */

const {setGlobalOptions} = require("firebase-functions");
const {onDocumentUpdated, onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onCall, HttpsError} = require("firebase-functions/v2/https");
const functionsV1 = require("firebase-functions/v1");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
const Groq = require("groq-sdk");
const pdfParse = require("pdf-parse");

admin.initializeApp();
const db = admin.firestore();

// Set global options for Cloud Functions v2
setGlobalOptions({maxInstances: 10, region: "asia-east1"});

/**
 * Helper: Call Groq API with Llama 3.3 model
 * @param {string} prompt Prompt content
 * @param {boolean} responseJsonMode Format as JSON
 * @return {Promise<string>} AI response text
 */
async function callGroqAI(prompt, responseJsonMode = true) {
  const apiKey = process.env.GROQ_API_KEY || process.env.GEMINI_API_KEY;
  if (!apiKey) {
    logger.error("GROQ_API_KEY is not configured in Firebase Secrets!");
    throw new Error("Missing GROQ_API_KEY secret config.");
  }

  const groq = new Groq({apiKey});

  const options = {
    messages: [
      {
        role: "user",
        content: prompt,
      },
    ],
    model: "llama-3.3-70b-versatile",
    temperature: 0.2,
    max_tokens: 1500,
  };

  if (responseJsonMode) {
    options.response_format = {type: "json_object"};
  }

  const chatCompletion = await groq.chat.completions.create(options);
  return chatCompletion.choices[0]?.message?.content || "";
}

/**
 * Helper: Parse JSON safely from AI response
 * @param {string} text Raw text output
 * @return {object} Parsed JSON object
 */
function parseJsonResult(text) {
  try {
    let cleanText = text.trim();
    if (cleanText.startsWith("```json")) {
      cleanText = cleanText.replace(/^```json/, "").replace(/```$/, "").trim();
    } else if (cleanText.startsWith("```")) {
      cleanText = cleanText.replace(/^```/, "").replace(/```$/, "").trim();
    }
    return JSON.parse(cleanText);
  } catch (err) {
    logger.error("Failed to parse JSON from AI output:", text, err);
    throw err;
  }
}

/**
 * Helper: Download PDF from Firebase Storage & extract raw text
 * @param {string} storagePathOrUrl Storage GS path or HTTP URL
 * @param {number} maxChars Max chars to truncate
 * @return {Promise<string>} Extracted text
 */
async function extractTextFromStoragePdf(storagePathOrUrl, maxChars = 4000) {
  try {
    let bucket = admin.storage().bucket();
    let file;

    if (storagePathOrUrl.startsWith("gs://")) {
      const parts = storagePathOrUrl.replace("gs://", "").split("/");
      const bucketName = parts.shift();
      const filePath = parts.join("/");
      bucket = admin.storage().bucket(bucketName);
      file = bucket.file(filePath);
    } else if (storagePathOrUrl.startsWith("http://") || storagePathOrUrl.startsWith("https://")) {
      const decodedUrl = decodeURIComponent(storagePathOrUrl);
      const match = decodedUrl.match(/\/o\/(.+?)\?/);
      if (match && match[1]) {
        file = bucket.file(match[1]);
      } else {
        const response = await fetch(storagePathOrUrl);
        const arrayBuffer = await response.arrayBuffer();
        const buffer = Buffer.from(arrayBuffer);
        const pdfData = await pdfParse(buffer);
        const text = pdfData.text || "";
        return text.length > maxChars ? text.slice(0, maxChars) + "\n...[Nội dung cắt ngắn]..." : text;
      }
    } else {
      file = bucket.file(storagePathOrUrl);
    }

    const [buffer] = await file.download();
    const pdfData = await pdfParse(buffer);
    const text = pdfData.text || "";
    return text.length > maxChars ? text.slice(0, maxChars) + "\n...[Nội dung cắt ngắn]..." : text;
  } catch (error) {
    logger.error(`Error reading PDF file from ${storagePathOrUrl}:`, error);
    throw error;
  }
}

// ============================================================================
// TASK 1: AI Gợi ý việc làm theo kỹ năng (Job Recommendation)
// Trigger: Khi field `skills` hoặc `profession` trong document `users/{userId}` thay đổi
// ============================================================================
exports.recommendJobsOnSkillUpdate = onDocumentUpdated({
  document: "users/{userId}",
  secrets: ["GROQ_API_KEY", "GEMINI_API_KEY"],
}, async (event) => {
  const userId = event.params.userId;
  const beforeData = event.data.before.data() || {};
  const afterData = event.data.after.data() || {};

  if (afterData.role && afterData.role !== "candidate") {
    return null;
  }

  const oldSkills = beforeData.skills || "";
  const newSkills = afterData.skills || "";
  const oldProf = beforeData.profession || "";
  const newProf = afterData.profession || "";

  if (oldSkills === newSkills && oldProf === newProf) {
    logger.info(`User ${userId} skills and profession did not change. Skipping.`);
    return null;
  }

  if (!newSkills.trim() && !newProf.trim()) {
    logger.info(`User ${userId} has no skills or profession listed.`);
    return null;
  }

  logger.info(`Generating job recommendations via Groq for user ${userId}`);

  try {
    const jobsSnapshot = await db.collection("jobs")
        .orderBy("id", "desc")
        .limit(20)
        .get();

    if (jobsSnapshot.empty) {
      logger.info("No jobs found in database to evaluate.");
      return null;
    }

    const jobsList = jobsSnapshot.docs.map((doc) => {
      const d = doc.data();
      return {
        jobId: doc.id || d.id,
        title: d.title || "",
        description: (d.description || "").slice(0, 500),
        requirements: (d.requirements || "").slice(0, 500),
      };
    });

    const prompt = `
Bạn là chuyên gia tư vấn tuyển dụng AI.
Thông tin ứng viên:
- Kỹ năng: "${newSkills}"
- Nghề nghiệp/Vị trí: "${newProf}"

Danh sách công việc cần đánh giá:
${JSON.stringify(jobsList, null, 2)}

Nhiệm vụ:
So sánh kỹ năng/nghề nghiệp của ứng viên với từng công việc. Chấm điểm độ phù hợp (từ 0 đến 100) và lý do ngắn gọn.
Trả về duy nhất JSON chứa key "matches" là một mảng theo đúng định dạng sau:
{
  "matches": [
    {
      "jobId": "...",
      "score": 85,
      "reason": "Kỹ năng Java và SQL của bạn rất khớp với yêu cầu công việc này."
    }
  ]
}
`;

    const rawResponse = await callGroqAI(prompt, true);
    const parsedObj = parseJsonResult(rawResponse);
    const matches = parsedObj.matches || parsedObj;

    if (!Array.isArray(matches)) {
      logger.error("Expected array response from Groq for recommendations", rawResponse);
      return null;
    }

    const batch = db.batch();
    const matchesRef = db.collection("job_matches").doc(userId).collection("matches");

    for (const match of matches) {
      if (!match.jobId) continue;
      const docRef = matchesRef.doc(match.jobId);
      batch.set(docRef, {
        jobId: match.jobId,
        score: Number(match.score) || 0,
        reason: match.reason || "",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      }, {merge: true});
    }

    await batch.commit();
    logger.info(`Successfully written ${matches.length} job match scores for user ${userId} via Groq`);
    return null;
  } catch (error) {
    logger.error(`Error in recommendJobsOnSkillUpdate for user ${userId}:`, error);
    return null;
  }
});

// ============================================================================
// TASK 2: AI Xếp hạng ứng viên (Applicant Ranking)
// Trigger: Khi có document mới trong collection `applications/{appId}`
// ============================================================================
exports.rankApplicantOnApplication = onDocumentCreated({
  document: "applications/{appId}",
  secrets: ["GROQ_API_KEY", "GEMINI_API_KEY"],
}, async (event) => {
  const appId = event.params.appId;
  const appData = event.data.data() || {};

  const jobId = appData.jobId;
  const candidateId = appData.candidateId;
  const cvUrl = appData.cvUrl;

  if (!jobId || !cvUrl) {
    logger.warn(`Application ${appId} missing jobId or cvUrl. Skipping ranking.`);
    return null;
  }

  try {
    const jobDoc = await db.collection("jobs").doc(jobId).get();
    if (!jobDoc.exists) {
      logger.error(`Job ${jobId} does not exist for application ${appId}`);
      return null;
    }
    const jobData = jobDoc.data();

    // Fetch candidate profile as fallback if PDF can't be extracted
    let userSkills = "";
    let userProfession = "";
    if (candidateId) {
      try {
        const userDoc = await db.collection("users").doc(candidateId).get();
        if (userDoc.exists) {
          const userData = userDoc.data();
          userSkills = userData.skills || "";
          userProfession = userData.profession || "";
        }
      } catch (err) {
        logger.warn(`Could not fetch user profile for ${candidateId}:`, err);
      }
    }

    let cvText = "";
    let cvSource = "CV PDF";
    try {
      cvText = await extractTextFromStoragePdf(cvUrl, 4000);
      logger.info(`Extracted CV text length: ${cvText.length} chars. Preview: ${cvText.slice(0, 200)}`);
    } catch (err) {
      logger.error(`Failed to extract text from CV URL ${cvUrl}:`, err);
    }

    // If PDF is empty (scan/image-based PDF), fall back to user profile data
    if (!cvText || cvText.trim().length < 50) {
      logger.warn(`CV text too short (${cvText.length} chars). Falling back to user profile data.`);
      if (userSkills || userProfession) {
        cvText = `Thông tin hồ sơ ứng viên (không thể đọc CV PDF):\n- Kỹ năng: ${userSkills}\n- Nghề nghiệp/Vị trí: ${userProfession}`;
        cvSource = "Hồ sơ hệ thống (PDF không đọc được)";
      } else {
        cvText = "Không có thông tin CV hoặc hồ sơ ứng viên.";
      }
    }

    const prompt = `
Bạn là hệ thống AI hỗ trợ Nhà tuyển dụng đánh giá ứng viên.
Thông tin công việc tuyển dụng:
- Tiêu đề: "${jobData.title || ""}"
- Mô tả công việc: "${(jobData.description || "").slice(0, 1000)}"
- Yêu cầu: "${(jobData.requirements || "").slice(0, 1000)}"

Thông tin ứng viên (nguồn: ${cvSource}):
"""
${cvText}
"""

Nhiệm vụ:
So sánh thông tin ứng viên với yêu cầu công việc. Hãy đánh giá điểm số phù hợp (score từ 0 đến 100) và giải thích lý do ngắn gọn bằng tiếng Việt.
QUAN TRỌNG: Dù thông tin ứng viên ít, hãy đưa ra đánh giá tốt nhất có thể với score từ 1-100 (không trả về 0 trừ khi rõ ràng không phù hợp).
Trả về duy nhất JSON theo đúng định dạng sau:
{
  "jobId": "${jobId}",
  "score": 75,
  "reason": "Ứng viên có kỹ năng phù hợp với vị trí này."
}
`;

    const rawResponse = await callGroqAI(prompt, true);
    logger.info(`Groq raw response for app ${appId}: ${rawResponse.slice(0, 300)}`);
    const evaluation = parseJsonResult(rawResponse);

    const aiScore = Number(evaluation.score) || 0;
    const aiReason = evaluation.reason || "Đã hoàn thành đánh giá.";

    await db.collection("applications").doc(appId).update({
      aiScore: aiScore,
      aiReason: aiReason,
      rankedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    logger.info(`Application ${appId} successfully ranked via Groq: Score ${aiScore}`);
    return null;
  } catch (error) {
    logger.error(`Error ranking applicant for app ${appId}:`, error);
    return null;
  }
});

// ============================================================================
// TASK 3: AI Phân tích CV PDF (CV Parsing)
// Trigger 1: Storage Upload Finalize
// Trigger 2: HTTPS Callable Function
// ============================================================================
exports.parseCvOnUpload = functionsV1.region("asia-east1")
    .runWith({secrets: ["GROQ_API_KEY", "GEMINI_API_KEY"]})
    .storage.object().onFinalize(async (object) => {
      const filePath = object.name;
      const contentType = object.contentType;

      if (!filePath.startsWith("cv/")) {
        return null;
      }

      if (contentType && !contentType.includes("pdf") && !filePath.endsWith(".pdf")) {
        logger.info(`File ${filePath} is not a PDF. Skipping parsing.`);
        return null;
      }

      logger.info(`Processing CV upload trigger via Groq for file: ${filePath}`);

      const pathParts = filePath.split("/");
      let userId = pathParts[pathParts.length - 1].replace(".pdf", "");
      if (pathParts.length > 2) {
        userId = pathParts[1];
      }

      try {
        const cvText = await extractTextFromStoragePdf(filePath, 4000);
        const prompt = `
Bạn là trợ lý AI trích xuất thông tin CV tự động.
Nội dung CV PDF:
"""
${cvText}
"""

Nhiệm vụ: Trích xuất các thông tin chính của ứng viên dưới dạng JSON chuẩn theo định dạng sau:
{
  "fullName": "Họ và tên ứng viên",
  "skills": "Chuỗi danh sách các kỹ năng phân cách bằng dấu phẩy, ví dụ: Java, Android, SQL, Git",
  "profession": "Vị trí hoặc Nghề nghiệp mong muốn, ví dụ: Lập trình viên Android"
}
`;

        const rawResponse = await callGroqAI(prompt, true);
        const parsedData = parseJsonResult(rawResponse);

        await db.collection("cv_parse_results").doc(userId).set({
          fullName: parsedData.fullName || "",
          skills: parsedData.skills || "",
          profession: parsedData.profession || "",
          filePath: filePath,
          parsedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, {merge: true});

        // Task 4: Tự động cập nhật trực tiếp vào profile người dùng
        if (userId && userId !== "unknown_user") {
          const userUpdates = {};
          if (parsedData.fullName && parsedData.fullName.trim()) userUpdates.fullName = parsedData.fullName.trim();
          if (parsedData.skills && parsedData.skills.trim()) userUpdates.skills = parsedData.skills.trim();
          if (parsedData.profession && parsedData.profession.trim()) userUpdates.profession = parsedData.profession.trim();

          if (Object.keys(userUpdates).length > 0) {
            await db.collection("users").doc(userId).set(userUpdates, {merge: true});
            logger.info(`Auto-updated profile for user ${userId} with CV parsed data.`);
          }
        }
        return null;
      } catch (error) {
        logger.error(`Error parsing CV PDF for file ${filePath}:`, error);
        return null;
      }
    });

exports.parseCvPdf = onCall({
  secrets: ["GROQ_API_KEY", "GEMINI_API_KEY"],
}, async (request) => {
  const {cvUrl, filePath, userId} = request.data || {};
  const targetUserId = userId || (request.auth ? request.auth.uid : "unknown_user");
  const targetPath = cvUrl || filePath;

  if (!targetPath) {
    throw new HttpsError("invalid-argument", "Thiếu cvUrl hoặc filePath trong request.");
  }

  logger.info(`Callable parseCvPdf called via Groq by user ${targetUserId} for ${targetPath}`);

  try {
    const cvText = await extractTextFromStoragePdf(targetPath, 4000);
    const prompt = `
Bạn là trợ lý AI trích xuất thông tin CV tự động.
Nội dung CV PDF:
"""
${cvText}
"""

Nhiệm vụ: Trích xuất thông tin dưới dạng JSON chuẩn:
{
  "fullName": "Họ và tên",
  "skills": "Kỹ năng (ví dụ: Java, SQL, Android)",
  "profession": "Vị trí / Nghề nghiệp mong muốn"
}
`;

    const rawResponse = await callGroqAI(prompt, true);
    const parsedData = parseJsonResult(rawResponse);

    const resultObj = {
      fullName: parsedData.fullName || "",
      skills: parsedData.skills || "",
      profession: parsedData.profession || "",
      parsedAt: new Date().toISOString(),
    };

    if (targetUserId && targetUserId !== "unknown_user") {
      await db.collection("cv_parse_results").doc(targetUserId).set(resultObj, {merge: true});

      // Task 4: Tự động cập nhật trực tiếp vào profile người dùng
      const userUpdates = {};
      if (resultObj.fullName && resultObj.fullName.trim()) userUpdates.fullName = resultObj.fullName.trim();
      if (resultObj.skills && resultObj.skills.trim()) userUpdates.skills = resultObj.skills.trim();
      if (resultObj.profession && resultObj.profession.trim()) userUpdates.profession = resultObj.profession.trim();

      if (Object.keys(userUpdates).length > 0) {
        await db.collection("users").doc(targetUserId).set(userUpdates, {merge: true});
        logger.info(`Callable parseCvPdf auto-updated profile for user ${targetUserId}`);
      }
    }

    return resultObj;
  } catch (error) {
    logger.error("Error in parseCvPdf callable:", error);
    throw new HttpsError("internal", error.message || "Không thể phân tích file PDF.");
  }
});
