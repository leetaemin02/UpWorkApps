package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.ApplicationWithJob;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.AppliedJobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppliedJobsFragment extends BaseFragment {

    private RecyclerView rvAppliedJobs;
    private AppliedJobAdapter adapter;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private List<ApplicationWithJob> allApplications = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_fragment_applications;
    }

    @Override
    protected void initViews(View view) {
        rvAppliedJobs = view.findViewById(R.id.rvAppliedJobs);
        tabLayout = view.findViewById(R.id.tabLayout);
        sessionManager = new SessionManager(getContext());
        db = FirebaseFirestore.getInstance();

        adapter = new AppliedJobAdapter(new ArrayList<>());
        rvAppliedJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppliedJobs.setAdapter(adapter);

        loadApplications();
    }

    private void loadApplications() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) {
            showToast("Vui lòng đăng nhập để xem đơn ứng tuyển");
            return;
        }

        db.collection("applications")
                .whereEqualTo("candidateId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allApplications.clear();
                    int total = queryDocumentSnapshots.size();
                    if (total == 0) {
                        adapter.setData(new ArrayList<>());
                        return;
                    }

                    java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(0);
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        com.example.jobsearchapp.data.models.Application app = doc.toObject(com.example.jobsearchapp.data.models.Application.class);
                        app.setId(doc.getId());
                        
                        ApplicationWithJob item = new ApplicationWithJob();
                        item.application = app;
                        
                        // Lấy chi tiết Job từ Firestore
                        db.collection("jobs").document(app.getJobId()).get()
                                .addOnSuccessListener(jobDoc -> {
                                    if (jobDoc.exists()) {
                                        item.job = mapDocToJob(jobDoc);
                                    }
                                    allApplications.add(item);
                                    if (counter.incrementAndGet() == total) {
                                        adapter.setData(allApplications);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }

    private Job mapDocToJob(com.google.firebase.firestore.DocumentSnapshot doc) {
        Job job = new Job();
        job.setId(doc.getId());
        job.setTitle(doc.getString("title"));
        job.setCompanyName(doc.getString("companyName"));
        job.setLocation(doc.getString("location"));
        job.setDeadlineFromObject(doc.get("deadline"));
        job.setPostedAtFromObject(doc.get("postedAt"));
        if (doc.contains("createdAt")) {
            job.setPostedAtFromObject(doc.get("createdAt"));
        }
        return job;
    }

    @Override
    protected void initListeners() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterByStatus(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterByStatus(String tabText) {
        String statusFilter;
        switch (tabText) {
            case "Đang xem xét": statusFilter = "pending"; break;
            case "Phỏng vấn": statusFilter = "reviewed"; break;
            case "Từ chối": statusFilter = "rejected"; break;
            default: statusFilter = "Tất cả"; break;
        }

        if (statusFilter.equals("Tất cả")) {
            adapter.setData(allApplications);
        } else {
            List<ApplicationWithJob> filteredList = new ArrayList<>();
            for (ApplicationWithJob item : allApplications) {
                if (item.application.getStatus().equalsIgnoreCase(statusFilter)) {
                    filteredList.add(item);
                }
            }
            adapter.setData(filteredList);
        }
    }
}