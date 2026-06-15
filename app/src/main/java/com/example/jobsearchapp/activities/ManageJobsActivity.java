package com.example.jobsearchapp.activities;

import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;

public class ManageJobsActivity extends BaseActivity {

    private RecyclerView rvJobs;
    private Button btnBack;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_manage_jobs;
    }

    @Override
    protected void initViews() {
        rvJobs = findViewById(R.id.rvJobs);
        btnBack = findViewById(R.id.btnBack);
        
        if (rvJobs != null) {
            rvJobs.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    protected void initListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}