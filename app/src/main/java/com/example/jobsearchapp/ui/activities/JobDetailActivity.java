package com.example.jobsearchapp.ui.activities;

import android.view.View;
import android.widget.TextView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.SavedJob;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends BaseActivity {

    private Job job;
    private boolean isFinishing = false;
    private TextView tvTitle, tvSalary, tvLocation, tvDesc, tvReq, tvDetailCompany, tvExp, tvDeadline, tvFullAddress;
    private android.widget.ImageView ivDetailLogo;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private AppDatabase appDatabase;
    private boolean isSaved = false;

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
        tvDetailCompany = findViewById(R.id.tvDetailCompany);
        tvExp = findViewById(R.id.tvDetailExp);
        tvDeadline = findViewById(R.id.tvDetailDeadline);
        tvFullAddress = findViewById(R.id.tvDetailFullAddress);
        ivDetailLogo = findViewById(R.id.ivDetailLogo);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        appDatabase = AppDatabase.getInstance(this);

        // Nhận dữ liệu từ Intent
        try {
            job = (Job) getIntent().getSerializableExtra("JOB_DATA");
        } catch (Exception e) {
            android.util.Log.e("JobDetail", "Error casting JOB_DATA", e);
            job = null;
        }

        if (job != null) {
            displayJobInfo();
            checkIfJobIsSaved();
        } else {
            // Log lỗi để debug
            android.util.Log.e("JobDetail", "Job data is null from Intent");
            showToast("Lỗi: Không thể tải thông tin công việc");
            isFinishing = true;
            finish();
        }
    }

    private void displayJobInfo() {
        if (job == null) return;

        if (tvTitle != null) tvTitle.setText(job.getTitle());
        if (tvSalary != null) tvSalary.setText(job.getSalaryMin() + " - " + job.getSalaryMax());
        if (tvLocation != null) tvLocation.setText(job.getLocation());
        if (tvDesc != null) tvDesc.setText(job.getDescription());
        if (tvReq != null) tvReq.setText(job.getRequirements());
        if (tvDetailCompany != null) tvDetailCompany.setText(job.getCompanyName());
        if (tvExp != null) tvExp.setText(job.getExperienceRequired());
        if (tvFullAddress != null) tvFullAddress.setText(job.getLocation()); // Or address if you have it

        if (ivDetailLogo != null && job.getLogoUrl() != null && !job.getLogoUrl().isEmpty()) {
            // If you have a library like Glide or Picasso, use it here. 
            // For now, it stays with default or you can add image loading logic.
        }

        // Tính toán đếm ngược deadline
        if (tvDeadline != null) {
            long diff = job.getDeadline() - System.currentTimeMillis();
            if (diff > 0) {
                long days = diff / (24 * 60 * 60 * 1000);
                tvDeadline.setText("Hạn nộp: Còn " + days + " ngày");
            } else {
                tvDeadline.setText("Hạn nộp: Đã hết hạn");
            }
        }
    }

    private void checkIfJobIsSaved() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection("saved_jobs").document(userId + "_" + job.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    isSaved = documentSnapshot.exists();
                    updateHeartIcon();
                });
    }

    private void updateHeartIcon() {
        android.widget.ImageView ivHeart = findViewById(R.id.ivHeart);
        if (ivHeart != null) {
            ivHeart.setImageResource(isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    protected void initListeners() {
        // Không khởi tạo listeners nếu activity đang đóng do lỗi
        if (isFinishing) return;

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

                // Tạo đơn ứng tuyển mới
                Application app = new Application(job.getId(), userId, job.getCompanyId(), job.getEmployerId());
                app.setJobTitle(job.getTitle());
                app.setStatus("pending");
                app.setAppliedAt(System.currentTimeMillis());

                db.collection("applications")
                        .add(app)
                        .addOnSuccessListener(documentReference -> {
                            showToast("Đã gửi yêu cầu ứng tuyển thành công!");
                            finish();
                        })
                        .addOnFailureListener(e -> showToast("Lỗi ứng tuyển: " + e.getMessage()));
            });
        }

        View ivHeart = findViewById(R.id.ivHeart);
        if (ivHeart != null) {
            ivHeart.setOnClickListener(v -> {
                String userId = sessionManager.getUserId();
                if (userId.isEmpty()) {
                    showToast("Vui lòng đăng nhập");
                    return;
                }

                if (job == null) return;

                if (!isSaved) {
                    SavedJob savedJob = new SavedJob(userId, job.getId());
                    db.collection("saved_jobs").document(userId + "_" + job.getId())
                            .set(savedJob)
                            .addOnSuccessListener(aVoid -> {
                                isSaved = true;
                                updateHeartIcon();
                                showToast("Đã lưu công việc");
                            });
                } else {
                    db.collection("saved_jobs").document(userId + "_" + job.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                isSaved = false;
                                updateHeartIcon();
                                showToast("Đã bỏ lưu");
                            });
                }
            });
        }
    }
}
