package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Applicant;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Notification;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

public class ApplicantDetailActivity extends BaseActivity {

    private TextView tvName, tvEmail, tvPhone, tvJobTitle;
    private ChipGroup cgSkills;
    private android.widget.ImageView ivAvatar;
    private View btnViewCv, btnViewJob, layoutActions;
    private View btnAccept, btnReject;
    private TextView tvProcessedStatus;
    private Applicant applicant;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_applicant_detail;
    }

    @Override
    protected void initViews() {
        tvName = findViewById(R.id.tvApplicantName);
        tvEmail = findViewById(R.id.tvApplicantEmail);
        tvPhone = findViewById(R.id.tvApplicantPhone);
        tvJobTitle = findViewById(R.id.tvAppliedJobTitle);
        cgSkills = findViewById(R.id.cgApplicantSkills);
        ivAvatar = findViewById(R.id.ivApplicantAvatar);
        btnViewCv = findViewById(R.id.btnViewCv);
        btnViewJob = findViewById(R.id.btnViewAppliedJob);
        layoutActions = findViewById(R.id.layoutDetailActions);
        btnAccept = findViewById(R.id.btnDetailAccept);
        btnReject = findViewById(R.id.btnDetailReject);
        tvProcessedStatus = findViewById(R.id.tvDetailProcessedStatus);

        db = FirebaseFirestore.getInstance();

        applicant = (Applicant) getIntent().getSerializableExtra("APPLICANT_DATA");

        if (applicant != null) {
            displayBasicInfo();
            loadFullProfile();
        } else {
            showToast("Không tìm thấy dữ liệu ứng viên");
            finish();
        }
    }

    private void displayBasicInfo() {
        tvName.setText(applicant.getName());
        tvEmail.setText("Email: " + applicant.getEmail());
        tvJobTitle.setText("Ứng tuyển: " + applicant.getJobTitle());

        if (applicant.getAvatarUrl() != null && !applicant.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(applicant.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
        
        updateActionButtonsVisibility();

        btnViewCv.setOnClickListener(v -> {
            if (applicant.getCvUrl() != null && !applicant.getCvUrl().isEmpty()) {
                try {
                    // Sử dụng logic giống hệt ProfileFragment: Mở trực tiếp link trong trình duyệt
                    String url = applicant.getCvUrl();
                    if (!url.startsWith("http")) url = "https://" + url;
                    
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    showToast("Không thể mở CV: " + e.getMessage());
                }
            } else {
                showToast("Ứng viên này chưa nộp CV");
            }
        });

        btnViewJob.setOnClickListener(v -> {
            if (applicant.getJobId() != null && !applicant.getJobId().isEmpty()) {
                db.collection("jobs").document(applicant.getJobId()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Job job = doc.toObject(Job.class);
                            if (job != null) {
                                job.setId(doc.getId());
                                Intent intent = new Intent(this, JobDetailActivity.class);
                                intent.putExtra("JOB_DATA", job);
                                startActivity(intent);
                            }
                        } else {
                            showToast("Không tìm thấy thông tin công việc");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
            } else {
                showToast("Không tìm thấy ID công việc");
            }
        });
    }

    private void updateActionButtonsVisibility() {
        String status = applicant.getStatus();
        if ("pending".equalsIgnoreCase(status) || status == null || status.isEmpty()) {
            layoutActions.setVisibility(View.VISIBLE);
            tvProcessedStatus.setVisibility(View.GONE);
        } else {
            layoutActions.setVisibility(View.GONE);
            tvProcessedStatus.setVisibility(View.VISIBLE);
            String statusText = "Hồ sơ này đã được xử lý: " + 
                ("Accepted".equalsIgnoreCase(status) ? "Phỏng vấn" : "Từ chối");
            tvProcessedStatus.setText(statusText);
        }
    }

    private void updateStatus(String newStatus) {
        db.collection("applications").document(applicant.getApplicationId())
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                applicant.setStatus(newStatus);
                updateActionButtonsVisibility();
                
                // Gửi thông báo
                String statusVN = "Accepted".equalsIgnoreCase(newStatus) ? "Phỏng vấn" : "Từ chối";
                Notification notif = new Notification(
                        applicant.getId(),
                        "Cập nhật trạng thái đơn ứng tuyển",
                        "Đơn ứng tuyển vị trí " + applicant.getJobTitle() + " của bạn đã được cập nhật: " + statusVN,
                        "status_change",
                        applicant.getJobId(),
                        applicant.getApplicationId()
                );
                db.collection("notifications").add(notif);
                
                showToast("Đã " + (newStatus.equals("Accepted") ? "chấp nhận" : "từ chối") + " ứng viên");
            })
            .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }

    private void loadFullProfile() {
        db.collection("users").document(applicant.getId()).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        tvPhone.setText("Số điện thoại: " + (user.getPhone() != null ? user.getPhone() : "Chưa cập nhật"));
                        loadSkills(user.getSkills());
                        // Có thể load ảnh avatar ở đây nếu dùng Glide
                    }
                }
            });
    }

    private void loadSkills(String skillsStr) {
        cgSkills.removeAllViews();
        if (skillsStr != null && !skillsStr.isEmpty()) {
            for (String skill : skillsStr.split(",")) {
                if (skill.trim().isEmpty()) continue;
                Chip chip = new Chip(this);
                chip.setText(skill.trim());
                cgSkills.addView(chip);
            }
        }
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        if (btnAccept != null) btnAccept.setOnClickListener(v -> updateStatus("Accepted"));
        if (btnReject != null) btnReject.setOnClickListener(v -> updateStatus("Rejected"));
    }
}