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
    private com.google.firebase.firestore.ListenerRegistration applicationsListener;

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

        // Hủy listener cũ nếu có
        if (applicationsListener != null) {
            applicationsListener.remove();
        }

        // Lắng nghe thay đổi danh sách đơn ứng tuyển trong thời gian thực
        applicationsListener = db.collection("applications")
                .whereEqualTo("candidateId", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        showToast("Lỗi lắng nghe dữ liệu: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    List<ApplicationWithJob> updatedList = new ArrayList<>();
                    int total = queryDocumentSnapshots.size();
                    if (total == 0) {
                        allApplications.clear();
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
                                    updatedList.add(item);
                                    if (counter.incrementAndGet() == total) {
                                        allApplications = new ArrayList<>(updatedList);
                                        // Sau khi cập nhật allApplications, áp dụng filter hiện tại
                                        int selectedTab = tabLayout.getSelectedTabPosition();
                                        filterByStatus(tabLayout.getTabAt(selectedTab).getText().toString());
                                    }
                                });
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (applicationsListener != null) {
            applicationsListener.remove();
        }
    }

    private Job mapDocToJob(com.google.firebase.firestore.DocumentSnapshot doc) {
        Job job = doc.toObject(Job.class);
        if (job != null) {
            job.setId(doc.getId());
            // Firestore mapping có thể không xử lý tốt một số field nếu dùng toObject trực tiếp với class này,
            // nên ta đảm bảo lấy các field quan trọng bằng phương thức an toàn
            job.setDeadlineFromObject(doc.get("deadline"));
            job.setPostedAtFromObject(doc.get("postedAt"));
            if (doc.contains("createdAt")) {
                job.setPostedAtFromObject(doc.get("createdAt"));
            }
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
        if ("Tất cả".equals(tabText)) {
            adapter.setData(allApplications);
            return;
        }

        List<ApplicationWithJob> filteredList = new ArrayList<>();
        for (ApplicationWithJob item : allApplications) {
            String status = item.application.getStatus();
            if (status == null) continue;

            boolean matches = false;
            switch (tabText) {
                case "Đang xem xét":
                    matches = "pending".equalsIgnoreCase(status);
                    break;
                case "Phỏng vấn":
                    // Phỏng vấn bao gồm cả status 'reviewed' và 'accepted'
                    matches = "reviewed".equalsIgnoreCase(status) || "accepted".equalsIgnoreCase(status);
                    break;
                case "Từ chối":
                    matches = "rejected".equalsIgnoreCase(status);
                    break;
            }

            if (matches) {
                filteredList.add(item);
            }
        }
        adapter.setData(filteredList);
    }
}