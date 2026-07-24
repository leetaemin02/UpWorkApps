package com.example.jobsearchapp.ui.activities;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.DataSeedHelper;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PostJobActivity extends BaseActivity {

    private EditText edtJobName, edtSalary, edtLocation, edtDescription;
    private EditText edtJobType, edtCategory, edtExperience, edtQuantity, edtDeadline, edtRequirements, edtBenefits;
    private Button btnSave, btnBulkUpload;
    private ImageView ivBack;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity_post_job;
    }

    @Override
    protected void initViews() {
        edtJobName = findViewById(R.id.edtJobName);
        edtSalary = findViewById(R.id.edtSalary);
        edtLocation = findViewById(R.id.edtLocation);
        edtDescription = findViewById(R.id.edtDescription);
        edtJobType = findViewById(R.id.edtJobType);
        edtCategory = findViewById(R.id.edtCategory);
        edtExperience = findViewById(R.id.edtExperience);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtDeadline = findViewById(R.id.edtDeadline);
        edtRequirements = findViewById(R.id.edtRequirements);
        edtBenefits = findViewById(R.id.edtBenefits);
        btnSave = findViewById(R.id.btnSave);
        btnBulkUpload = findViewById(R.id.btnBulkUpload);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
    }

    @Override
    protected void initListeners() {
        ivBack.setOnClickListener(v -> finish());

        if (btnBulkUpload != null) {
            btnBulkUpload.setOnClickListener(v -> {
                DataSeedHelper.pushMockJobsToFirebase(this);
            });
        }

        btnSave.setOnClickListener(v -> {
            String jobTitle = edtJobName.getText().toString().trim();
            String salaryStr = edtSalary.getText().toString().trim();
            String location = edtLocation.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();
            String jobType = edtJobType.getText().toString().trim();
            String category = edtCategory.getText().toString().trim();
            String experience = edtExperience.getText().toString().trim();
            String quantityStr = edtQuantity.getText().toString().trim();
            String deadlineStr = edtDeadline.getText().toString().trim();
            String requirements = edtRequirements.getText().toString().trim();
            String benefits = edtBenefits.getText().toString().trim();

            if (jobTitle.isEmpty() || salaryStr.isEmpty() || location.isEmpty()) {
                showToast("Vui lòng điền đầy đủ các trường bắt buộc");
                return;
            }

            long salary = 0;
            try {
                salary = Long.parseLong(salaryStr);
            } catch (Exception e) {}

            int quantity = 1;
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (Exception e) {}

            long deadline = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                deadline = sdf.parse(deadlineStr).getTime();
            } catch (Exception e) {}

            String userId = sessionManager.getUserId();
            if (userId.isEmpty()) {
                showToast("Lỗi xác thực");
                return;
            }

            Map<String, Object> job = new HashMap<>();
            job.put("companyId", userId);
            job.put("employerId", userId);
            job.put("title", jobTitle);
            job.put("salaryMin", salary);
            job.put("salaryMax", salary);
            job.put("location", location);
            job.put("description", description);
            job.put("jobType", jobType);
            job.put("category", category);
            job.put("experienceRequired", experience);
            job.put("quantity", quantity);
            job.put("deadline", deadline);
            job.put("requirements", requirements);
            job.put("benefits", benefits);
            job.put("postedAt", System.currentTimeMillis());
            job.put("status", "active");
            job.put("views", 0);

            db.collection("jobs")
                .add(job)
                .addOnSuccessListener(documentReference -> {
                    showToast("Đăng tin thành công!");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
        });
    }
}
