package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Notification;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.employer.adapters.ManageJobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageJobsActivity extends BaseActivity implements ManageJobAdapter.OnJobActionListener {

    private RecyclerView rvJobs;
    private ImageView ivBack;
    private FloatingActionButton fabAddJob;
    private ManageJobAdapter adapter;
    private List<Job> jobList = new ArrayList<>();
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity_manage_jobs;
    }

    @Override
    protected void initViews() {
        rvJobs = findViewById(R.id.rvJobs);
        ivBack = findViewById(R.id.ivBack);
        fabAddJob = findViewById(R.id.fabAddJob);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageJobAdapter(jobList, this);
        rvJobs.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJobs();
    }

    private void loadJobs() {
        String employerId = sessionManager.getUserId();
        if (!employerId.isEmpty()) {
            db.collection("jobs")
                .whereEqualTo("employerId", employerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Job> jobs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        jobs.add(mapDocToJob(doc));
                    }
                    jobList = jobs;
                    adapter.updateList(jobList);
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
        }
    }

    private Job mapDocToJob(com.google.firebase.firestore.DocumentSnapshot doc) {
        Job job = new Job();
        job.setId(doc.getId());
        job.setCompanyId(doc.getString("companyId"));
        job.setEmployerId(doc.getString("employerId"));
        job.setTitle(doc.getString("title"));
        job.setDescription(doc.getString("description"));
        job.setRequirements(doc.getString("requirements"));
        job.setBenefits(doc.getString("benefits"));
        
        Long sMin = doc.getLong("salaryMin");
        job.setSalaryMin(sMin != null ? sMin : 0);
        
        Long sMax = doc.getLong("salaryMax");
        job.setSalaryMax(sMax != null ? sMax : 0);
        
        job.setLocation(doc.getString("location"));
        job.setJobType(doc.getString("jobType"));
        job.setCategory(doc.getString("category"));
        job.setExperienceRequired(doc.getString("experienceRequired"));
        job.setStatus(doc.getString("status"));
        
        job.setDeadlineFromObject(doc.get("deadline"));
        job.setPostedAtFromObject(doc.get("postedAt"));
        if (doc.contains("createdAt")) {
            job.setPostedAtFromObject(doc.get("createdAt"));
        }
        
        job.setCompanyName(doc.getString("companyName"));
        job.setLogoUrl(doc.getString("logoUrl"));
        return job;
    }

    @Override
    protected void initListeners() {
        ivBack.setOnClickListener(v -> finish());
        fabAddJob.setOnClickListener(v -> startActivity(new Intent(this, PostJobActivity.class)));
    }

    @Override
    public void onEdit(Job job) {
        Intent intent = new Intent(this, EditJobActivity.class);
        intent.putExtra("JOB_DATA", job);
        startActivity(intent);
    }

    @Override
    public void onDelete(Job job) {
        String jobTitle = job.getTitle();
        db.collection("jobs").document(job.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                // Thông báo cho nhà tuyển dụng
                Notification notif = new Notification(
                        sessionManager.getUserId(),
                        "Xóa tin thành công",
                        "Bạn đã xóa tin tuyển dụng vị trí " + jobTitle + " thành công",
                        "system",
                        null,
                        null
                );
                db.collection("notifications").add(notif);

                showToast("Đã xóa tin tuyển dụng");
                loadJobs();
            })
            .addOnFailureListener(e -> showToast("Lỗi xóa: " + e.getMessage()));
    }
}
