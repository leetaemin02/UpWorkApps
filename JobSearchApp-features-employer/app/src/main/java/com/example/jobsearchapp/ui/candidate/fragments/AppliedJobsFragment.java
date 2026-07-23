package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.ApplicationWithJob;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.AppliedJobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

public class AppliedJobsFragment extends BaseFragment {

    private RecyclerView rvAppliedJobs;
    private AppliedJobAdapter adapter;
    private TabLayout tabLayout;
    private SessionManager sessionManager;
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

        adapter = new AppliedJobAdapter(new ArrayList<>());
        rvAppliedJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAppliedJobs.setAdapter(adapter);

        loadApplications();
    }

    private void loadApplications() {
        String userId = sessionManager.getUserId();
        if (!userId.isEmpty()) {
            allApplications = AppDatabase.getInstance(getContext()).applicationDao().getMyApplicationsWithJob(userId);
            adapter.setData(allApplications);
        } else {
            showToast("Vui lòng đăng nhập để xem đơn ứng tuyển");
        }
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

    private void filterByStatus(String status) {
        if (status.equals("Tất cả")) {
            adapter.setData(allApplications);
        } else {
            List<ApplicationWithJob> filteredList = new ArrayList<>();
            for (ApplicationWithJob item : allApplications) {
                if (item.application.getStatus().equals(status)) {
                    filteredList.add(item);
                }
            }
            adapter.setData(filteredList);
        }
    }
}