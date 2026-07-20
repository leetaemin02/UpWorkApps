package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.AppliedJobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppliedJobsFragment extends BaseFragment {

    private RecyclerView rvAppliedJobs;
    private AppliedJobAdapter adapter;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private List<Application> allApplications = new ArrayList<>();

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
    }

    @Override
    public void onResume() {
        super.onResume();
        loadApplications();
    }

    private void loadApplications() {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showToast("Vui lòng đăng nhập để xem đơn ứng tuyển");
            return;
        }

        db.collection("applications")
                .whereEqualTo("candidateId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allApplications.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Application app = doc.toObject(Application.class);
                        app.setId(doc.getId());
                        allApplications.add(app);
                    }
                    adapter.setData(allApplications);
                })
                .addOnFailureListener(e -> showToast("Lỗi tải đơn: " + e.getMessage()));
    }

    @Override
    protected void initListeners() {
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (tab.getText() != null) {
                        filterByStatus(tab.getText().toString());
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
        }
    }

    private void filterByStatus(String statusTab) {
        if (statusTab.equals("Tất cả")) {
            adapter.setData(allApplications);
        } else {
            List<Application> filteredList = new ArrayList<>();
            for (Application item : allApplications) {
                String dbStatus = item.getStatus();
                if (dbStatus != null) {

                    if (statusTab.equals("Đang xem xét") && (dbStatus.equalsIgnoreCase("pending") || dbStatus.equalsIgnoreCase("Đang xem xét"))) {
                        filteredList.add(item);
                    } else if (statusTab.equals("Phỏng vấn") && (dbStatus.equalsIgnoreCase("Accepted") || dbStatus.equalsIgnoreCase("Phỏng vấn"))) {
                        filteredList.add(item);
                    } else if (statusTab.equals("Từ chối") && (dbStatus.equalsIgnoreCase("Rejected") || dbStatus.equalsIgnoreCase("Từ chối"))) {
                        filteredList.add(item);
                    }
                }
            }
            adapter.setData(filteredList);
        }
    }
}