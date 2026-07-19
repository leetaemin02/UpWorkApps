package com.example.jobsearchapp.ui.activities;

import android.view.View;
import android.widget.TextView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.SavedJob;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends BaseActivity {

    private Job job;
    private TextView tvTitle, tvSalary, tvLocation, tvDesc, tvReq, tvCompany, tvExp;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private AppDatabase appDatabase;

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
        appDatabase = AppDatabase.getInstance(this);

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

                Application app = new Application(
                        job.getId(),
                        userId,
                        job.getCompanyId()
                );

                app.setJobTitle(job.getTitle());
                app.setStatus("pending");

                db.collection("applications")
                        .add(app)
                        .addOnSuccessListener(documentReference -> {

                            showToast("Đã gửi yêu cầu ứng tuyển cho: "
                                    + job.getTitle());

                            finish();

                        })
                        .addOnFailureListener(e ->
                                showToast("Lỗi ứng tuyển: " + e.getMessage()));
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

                SavedJob savedJob =
                        appDatabase.savedJobDao().getSaved(userId, job.getId());

                if (savedJob == null) {

                    SavedJob newSaved =
                            new SavedJob(userId, job.getId());

                    appDatabase.savedJobDao().addToSaved(newSaved);

                    showToast("Đã lưu công việc");

                } else {

                    appDatabase.savedJobDao().removeFromSaved(savedJob);

                    showToast("Đã bỏ lưu");

                }

            });

        }

    }

}