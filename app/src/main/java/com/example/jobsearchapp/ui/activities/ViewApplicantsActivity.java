package com.example.jobsearchapp.ui.activities;

import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Applicant;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Notification;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.employer.adapters.ApplicantAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewApplicantsActivity extends BaseActivity implements ApplicantAdapter.OnApplicantActionListener {

    private RecyclerView rvApplicants;
    private ImageView ivBack;
    private ApplicantAdapter adapter;
    private List<Applicant> applicantList = new ArrayList<>();
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration applicantsListener;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity_view_applicants;
    }

    @Override
    protected void initViews() {
        rvApplicants = findViewById(R.id.rvApplicants);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicantAdapter(applicantList, this);
        rvApplicants.setAdapter(adapter);

        loadApplicants();
    }

    private void loadApplicants() {
        String employerId = sessionManager.getUserId();
        if (employerId.isEmpty()) return;

        if (applicantsListener != null) applicantsListener.remove();

        applicantsListener = db.collection("applications")
                .whereEqualTo("companyId", employerId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        showToast("Lỗi tải ứng viên: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    List<Applicant> applicants = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        adapter.updateList(applicants);
                        return;
                    }

                    AtomicInteger counter = new AtomicInteger(0);
                    int total = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Application app = doc.toObject(Application.class);
                        if (app == null) {
                            checkAndUpdateList(counter, total, applicants);
                            continue;
                        }
                        app.setId(doc.getId());

                        if (app.getCandidateId() == null || app.getJobId() == null) {
                            checkAndUpdateList(counter, total, applicants);
                            continue;
                        }

                        db.collection("users").document(app.getCandidateId()).get()
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                                    User user = userTask.getResult().toObject(User.class);
                                    db.collection("jobs").document(app.getJobId()).get()
                                        .addOnCompleteListener(jobTask -> {
                                            if (jobTask.isSuccessful() && jobTask.getResult() != null && jobTask.getResult().exists()) {
                                                Job job = jobTask.getResult().toObject(Job.class);
                                                
                                                if (user != null) {
                                                    Applicant applicant = new Applicant();
                                                    applicant.setApplicationId(app.getId());
                                                    applicant.setId(userTask.getResult().getId());
                                                    applicant.setName(user.getFullName());
                                                    applicant.setEmail(user.getEmail());
                                                    applicant.setCvUrl(app.getCvUrl());
                                                    
                                                    // Ưu tiên lấy ảnh tại thời điểm nộp, nếu không có (đơn cũ) thì lấy ảnh hiện tại
                                                    String historicAvatar = app.getCandidateAvatarUrl();
                                                    applicant.setAvatarUrl(historicAvatar != null && !historicAvatar.isEmpty() ? historicAvatar : user.getAvatarUrl());
                                                    
                                                    applicant.setJobTitle(job != null ? job.getTitle() : "N/A");
                                                    applicant.setJobId(app.getJobId());
                                                    applicant.setStatus(app.getStatus());
                                                    applicant.setAiScore(app.getAiScore());
                                                    applicant.setAiReason(app.getAiReason());
                                                    applicants.add(applicant);
                                                }
                                            }
                                            checkAndUpdateList(counter, total, applicants);
                                        });
                                } else {
                                    checkAndUpdateList(counter, total, applicants);
                                }
                            });
                    }
                });
    }

    private void checkAndUpdateList(AtomicInteger counter, int total, List<Applicant> applicants) {
        if (counter.incrementAndGet() == total) {
            applicantList = applicants;
            adapter.updateList(applicantList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (applicantsListener != null) {
            applicantsListener.remove();
        }
    }

    @Override
    protected void initListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onAccept(Applicant applicant) {
        // Chỉ cập nhật trạng thái đơn ứng tuyển, không can thiệp vào số lượng bài đăng
        updateApplicationStatus(applicant.getApplicationId(), "Accepted");
    }

    @Override
    public void onReject(Applicant applicant) {
        updateApplicationStatus(applicant.getApplicationId(), "Rejected");
    }

    @Override
    public void onViewDetail(Applicant applicant) {
        android.content.Intent intent = new android.content.Intent(this, ApplicantDetailActivity.class);
        intent.putExtra("APPLICANT_DATA", applicant);
        startActivity(intent);
    }

    private void updateApplicationStatus(String applicationId, String status) {
        db.collection("applications").document(applicationId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String candidateId = doc.getString("candidateId");
                    String jobTitle = doc.getString("jobTitle");
                    String jobId = doc.getString("jobId");

                    db.collection("applications").document(applicationId)
                        .update("status", status)
                        .addOnSuccessListener(aVoid -> {
                            // Tạo thông báo kết quả cho Ứng viên
                            String statusVN = "pending".equalsIgnoreCase(status) ? "Đang xem xét" :
                                             "Rejected".equalsIgnoreCase(status) ? "Từ chối" : "Phỏng vấn";
                            
                            Notification notif = new Notification(
                                    candidateId,
                                    "Cập nhật trạng thái đơn ứng tuyển",
                                    "Đơn ứng tuyển vị trí " + jobTitle + " của bạn đã được cập nhật: " + statusVN,
                                    "status_change",
                                    jobId,
                                    applicationId
                            );
                            db.collection("notifications").add(notif);

                            showToast("Đã " + (status.equals("Accepted") ? "chấp nhận" : "từ chối") + " đơn ứng tuyển");
                            // Với addSnapshotListener, không cần gọi loadApplicants() thủ công nữa
                        });
                }
            })
            .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }
}
