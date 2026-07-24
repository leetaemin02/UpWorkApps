package com.example.jobsearchapp.ui.activities;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditJobActivity extends BaseActivity {

    private EditText edtJobName, edtSalaryMin, edtSalaryMax, edtLocation, edtDescription, edtRequirements, edtBenefits, edtJobType, edtCategory, edtDeadline;
    private Button btnUpdate;
    private ImageView ivBack;
    private FirebaseFirestore db;
    private Job currentJob;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity_edit_job;
    }

    @Override
    protected void initViews() {
        edtJobName = findViewById(R.id.edtJobName);
        edtSalaryMin = findViewById(R.id.edtSalaryMin);
        edtSalaryMax = findViewById(R.id.edtSalaryMax);
        edtLocation = findViewById(R.id.edtLocation);
        edtDescription = findViewById(R.id.edtDescription);
        edtRequirements = findViewById(R.id.edtRequirements);
        edtBenefits = findViewById(R.id.edtBenefits);
        edtJobType = findViewById(R.id.edtJobType);
        edtCategory = findViewById(R.id.edtCategory);
        edtDeadline = findViewById(R.id.edtDeadline);
        btnUpdate = findViewById(R.id.btnUpdate);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseFirestore.getInstance();
        
        currentJob = (Job) getIntent().getSerializableExtra("JOB_DATA");
        if (currentJob != null) {
            displayJobData();
        }
    }

    private void displayJobData() {
        edtJobName.setText(currentJob.getTitle());
        edtSalaryMin.setText(String.valueOf(currentJob.getSalaryMin()));
        edtSalaryMax.setText(String.valueOf(currentJob.getSalaryMax()));
        edtLocation.setText(currentJob.getLocation());
        edtDescription.setText(currentJob.getDescription());
        edtRequirements.setText(currentJob.getRequirements());
        edtBenefits.setText(currentJob.getBenefits());
        edtJobType.setText(currentJob.getJobType());
        edtCategory.setText(currentJob.getCategory());
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        edtDeadline.setText(sdf.format(new java.util.Date(currentJob.getDeadline())));
    }

    @Override
    protected void initListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnUpdate.setOnClickListener(v -> {
            if (currentJob == null) return;

            String title = edtJobName.getText().toString().trim();
            String sMin = edtSalaryMin.getText().toString().trim();
            String sMax = edtSalaryMax.getText().toString().trim();
            String loc = edtLocation.getText().toString().trim();
            
            if (title.isEmpty() || sMin.isEmpty() || sMax.isEmpty() || loc.isEmpty()) {
                showToast("Vui lòng điền đủ thông tin cơ bản");
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("title", title);
            updates.put("salaryMin", Long.parseLong(sMin));
            updates.put("salaryMax", Long.parseLong(sMax));
            updates.put("location", loc);
            updates.put("description", edtDescription.getText().toString().trim());
            updates.put("requirements", edtRequirements.getText().toString().trim());
            updates.put("benefits", edtBenefits.getText().toString().trim());
            updates.put("jobType", edtJobType.getText().toString().trim());
            updates.put("category", edtCategory.getText().toString().trim());

            String deadlineStr = edtDeadline.getText().toString().trim();
            long deadline = currentJob.getDeadline();
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                deadline = sdf.parse(deadlineStr).getTime();
            } catch (Exception e) {}
            updates.put("deadline", deadline);

            db.collection("jobs").document(currentJob.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showToast("Cập nhật tin thành công");
                    finish();
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
        });
    }
}
