package com.example.jobsearchapp.activities;

import android.content.Intent;
import android.widget.Button;

import com.example.jobsearchapp.R;

public class EmployerActivity extends BaseActivity {

    private Button btnPostJob, btnManageJob, btnViewApplicants;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_employer;
    }

    @Override
    protected void initViews() {
        btnPostJob = findViewById(R.id.btnPostJob);
        btnManageJob = findViewById(R.id.btnManageJob);
        btnViewApplicants = findViewById(R.id.btnApplicant);
    }

    @Override
    protected void initListeners() {
        btnPostJob.setOnClickListener(v ->
                startActivity(new Intent(this, PostJobActivity.class)));

        btnManageJob.setOnClickListener(v ->
                startActivity(new Intent(this, ManageJobsActivity.class)));

        btnViewApplicants.setOnClickListener(v ->
                startActivity(new Intent(this, ViewApplicantsActivity.class)));
    }
}