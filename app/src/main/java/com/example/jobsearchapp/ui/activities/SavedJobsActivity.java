package com.example.jobsearchapp.ui.activities;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.candidate.adapters.JobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SavedJobsActivity extends BaseActivity {

    private RecyclerView rvSavedJobs;
    private ProgressBar pbSavedJobs;
    private TextView tvNoSavedJobs;
    private JobAdapter adapter;
    private List<Job> savedJobList = new ArrayList<>();
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_saved_jobs;
    }

    @Override
    protected void initViews() {
        rvSavedJobs = findViewById(R.id.rvSavedJobs);
        pbSavedJobs = findViewById(R.id.pbSavedJobs);
        tvNoSavedJobs = findViewById(R.id.tvNoSavedJobs);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        adapter = new JobAdapter(savedJobList);
        rvSavedJobs.setLayoutManager(new LinearLayoutManager(this));
        rvSavedJobs.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedJobs();
    }

    private void loadSavedJobs() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) {
            finish();
            return;
        }

        pbSavedJobs.setVisibility(View.VISIBLE);
        db.collection("saved_jobs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        pbSavedJobs.setVisibility(View.GONE);
                        tvNoSavedJobs.setVisibility(View.VISIBLE);
                        savedJobList.clear();
                        adapter.updateList(new ArrayList<>());
                        return;
                    }

                    List<String> jobIds = new ArrayList<>();
                    Set<String> savedIdsSet = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String jId = doc.getString("jobId");
                        jobIds.add(jId);
                        savedIdsSet.add(jId);
                    }

                    adapter.setSavedJobIds(savedIdsSet);
                    fetchJobDetails(jobIds);
                })
                .addOnFailureListener(e -> {
                    pbSavedJobs.setVisibility(View.GONE);
                    showToast("Lỗi tải danh sách: " + e.getMessage());
                });
    }

    private void fetchJobDetails(List<String> jobIds) {
        List<Job> fetchedJobs = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);
        int total = jobIds.size();

        for (String id : jobIds) {
            db.collection("jobs").document(id).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Job job = mapDocToJob(doc);
                            fetchedJobs.add(job);
                        }
                        if (counter.incrementAndGet() == total) {
                            pbSavedJobs.setVisibility(View.GONE);
                            savedJobList = fetchedJobs;
                            adapter.updateList(savedJobList);
                            tvNoSavedJobs.setVisibility(savedJobList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
        }
    }

    private Job mapDocToJob(DocumentSnapshot doc) {
        Job job = doc.toObject(Job.class);
        if (job != null) {
            job.setId(doc.getId());
            job.setDeadlineFromObject(doc.get("deadline"));
            job.setPostedAtFromObject(doc.get("postedAt"));
        }
        return job;
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBackSaved).setOnClickListener(v -> finish());
    }
}
