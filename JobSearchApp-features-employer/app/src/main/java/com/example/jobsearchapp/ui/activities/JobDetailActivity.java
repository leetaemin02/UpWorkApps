package com.example.jobsearchapp.ui.activities;

import android.view.View;
import android.widget.TextView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends BaseActivity {

    private Job job;
    private TextView tvTitle, tvSalary, tvLocation, tvDesc, tvReq, tvCompany, tvExp;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_activity_job_detail;
    }

    @Override
    protected void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvSalary = findViewById(R.id.tvDetailSalary);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvReq = findViewById(R.id.tvDetailReq);
        tvCompany = findViewById(R.id.tvDetailCompany);
        tvExp = findViewById(R.id.tvDetailExp);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        // Nhận dữ liệu từ Intent
        job = (Job) getIntent().getSerializableExtra("JOB_DATA");
        if (job != null) {
            displayJobInfo();
        }
    }

    private void displayJobInfo() {
        tvTitle.setText(job.getTitle());
        tvSalary.setText(job.getSalaryMin() + " - " + job.getSalaryMax());
        tvLocation.setText(job.getLocation());
        tvDesc.setText(job.getDescription());
        tvReq.setText(job.getRequirements());
        tvCompany.setText(job.getCompanyName());
        tvExp.setText(job.getExperienceRequired());
    }

    @Override
    protected void initListeners() {
        View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        View btnApplyNow = findViewById(R.id.btnApplyNow);
        if (btnApplyNow != null) {
            btnApplyNow.setOnClickListener(v -> {
                String userId = sessionManager.getUserId();
                if (userId.isEmpty()) {
                    showToast("Vui lòng đăng nhập để ứng tuyển");
                    return;
                }

                if (job == null) return;

                // Lấy thêm tên của ứng viên từ Firestore để đồng bộ hiển thị sang tab Apps bên Candidate
                db.collection("users").document(userId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String candidateName = "Người ứng tuyển";
                            if (documentSnapshot.exists() && documentSnapshot.getString("fullName") != null) {
                                candidateName = documentSnapshot.getString("fullName");
                            }

                            // Tạo đơn ứng tuyển mới và gán đầy đủ thông tin (dùng currentTimeMillis cho long)
                            Application app = new Application(job.getId(), userId, job.getCompanyId());
                            app.setJobTitle(job.getTitle());
                            app.setCandidateName(candidateName);
                            app.setStatus("pending");
                            app.setAppliedAt(System.currentTimeMillis());

                            db.collection("applications")
                                    .add(app)
                                    .addOnSuccessListener(documentReference -> {
                                        showToast("Đã gửi yêu cầu ứng tuyển thành công!");
                                        finish();
                                    })
                                    .addOnFailureListener(e -> showToast("Lỗi ứng tuyển: " + e.getMessage()));
                        })
                        .addOnFailureListener(e -> {
                            // Nếu lỗi mạng, vẫn cố gắng gửi đơn với tên mặc định
                            Application app = new Application(job.getId(), userId, job.getCompanyId());
                            app.setJobTitle(job.getTitle());
                            app.setStatus("pending");
                            app.setAppliedAt(System.currentTimeMillis());

                            db.collection("applications").add(app)
                                    .addOnSuccessListener(ref -> {
                                        showToast("Đã gửi yêu cầu ứng tuyển thành công!");
                                        finish();
                                    });
                        });
            });
        }

        View ivHeart = findViewById(R.id.ivHeart);
        if (ivHeart != null) {
            ivHeart.setOnClickListener(v -> {
                showToast("Đã lưu công việc này");
            });
        }
    }
}