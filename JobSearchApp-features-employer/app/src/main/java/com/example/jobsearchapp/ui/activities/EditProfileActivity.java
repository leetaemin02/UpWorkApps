package com.example.jobsearchapp.ui.activities;

import android.util.Log;
import android.widget.EditText;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {
    private EditText edtFullName, edtPhone, edtCompany, edtProfession, edtLocation;
    private User currentUser;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_activity_edit_profile;
    }

    @Override
    protected void initViews() {
        edtFullName = findViewById(R.id.edtEditFullName);
        edtPhone = findViewById(R.id.edtEditPhone);
        edtCompany = findViewById(R.id.edtEditCompany);
        edtProfession = findViewById(R.id.edtEditProfession);
        edtLocation = findViewById(R.id.edtEditLocation);

        db = FirebaseFirestore.getInstance();
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        loadUserData();
    }

    private void loadUserData() {
        if (userId == null || userId.isEmpty()) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            edtFullName.setText(currentUser.getFullName());
                            edtPhone.setText(currentUser.getPhone());
                            edtCompany.setText(currentUser.getCompanyName());
                            edtProfession.setText(currentUser.getProfession());
                            edtLocation.setText(currentUser.getLocation());
                        }
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBackEdit).setOnClickListener(v -> finish());

        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            if (userId != null && !userId.isEmpty()) {
                String fullName = edtFullName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String company = edtEditCompanyVisible() ? edtCompany.getText().toString().trim() : "";
                String profession = edtProfession.getText().toString().trim();
                String location = edtLocation.getText().toString().trim();

                if (fullName.isEmpty()) {
                    showToast("Họ tên không được để trống");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", fullName);
                updates.put("phone", phone);
                updates.put("companyName", company);
                updates.put("profession", profession);
                updates.put("location", location);

                db.collection("users").document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            showToast("Đang cập nhật & Bơm dữ liệu lên Firebase...");
                            generateMockJobsAndApplications();
                        })
                        .addOnFailureListener(e -> showToast("Lỗi cập nhật: " + e.getMessage()));
            } else {
                showToast("Lỗi: Không tìm thấy ID người dùng");
            }
        });
    }

    private boolean edtEditCompanyVisible() {
        return edtCompany.getVisibility() == android.view.View.VISIBLE;
    }

    // --- HÀM TẠO DỮ LIỆU AN TOÀN, CÓ THÔNG BÁO HOÀN TẤT VÀ BẮT LỖI ---
    private void generateMockJobsAndApplications() {
        String employerId = userId != null ? userId : "employer_test_id";

        String[] jobTitles = {
                "Lập trình viên Android Junior", "Senior React Native Developer",
                "Nhân viên Sale Software", "Thực tập sinh Tester (QC)",
                "NodeJS Backend Engineer", "UI/UX Designer", "Flutter Developer",
                "Business Analyst (BA)", "Project Manager", "DevOps Engineer"
        };

        String[] locations = {"Hồ Chí Minh", "Hà Nội", "Đà Nẵng", "Cần Thơ", "Làm việc từ xa"};
        String[] candidateNames = {"Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", "Phạm Thị Dung", "Hoàng Văn Em"};

        for (int i = 1; i <= 30; i++) {
            final int index = i;
            String title = jobTitles[(int) (Math.random() * jobTitles.length)] + " #" + i;
            String location = locations[(int) (Math.random() * locations.length)];

            Map<String, Object> job = new HashMap<>();
            job.put("title", title);
            job.put("companyId", employerId);
            job.put("companyName", "Công ty TechCorp " + i);
            job.put("location", location);
            job.put("salaryMin", 8000000L);
            job.put("salaryMax", 18000000L);
            job.put("description", "Mô tả công việc chi tiết cho vị trí " + title);
            job.put("requirements", "Có từ 1 năm kinh nghiệm.");
            job.put("experienceRequired", "1 năm");
            job.put("createdAt", com.google.firebase.Timestamp.now());

            db.collection("jobs").add(job).addOnSuccessListener(documentReference -> {
                String generatedJobId = documentReference.getId();

                for (int c = 0; c < candidateNames.length; c++) {
                    if (Math.random() > 0.4) {
                        Map<String, Object> application = new HashMap<>();
                        application.put("jobId", generatedJobId);
                        application.put("jobTitle", title);
                        application.put("candidateId", "candidate_fake_" + c);
                        application.put("candidateName", candidateNames[c]);
                        application.put("employerId", employerId);
                        application.put("status", "pending");
                        application.put("appliedAt", com.google.firebase.Timestamp.now());

                        db.collection("applications").add(application);
                    }
                }

                if (index == 30) {
                    showToast("Đã bơm xong toàn bộ dữ liệu mẫu lên Firebase!");
                    finish();
                }
            }).addOnFailureListener(e -> {
                Log.e("FIRESTORE_ERROR", "Lỗi khi thêm job: " + e.getMessage());
            });
        }
    }
}