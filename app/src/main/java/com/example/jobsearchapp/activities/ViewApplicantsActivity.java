package com.example.jobsearchapp.activities;

import android.widget.Button;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;

public class ViewApplicantsActivity extends BaseActivity {

    private RecyclerView rvApplicants;
    private Button btnBack;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_view_applicants;
    }

    @Override
    protected void initViews() {
        rvApplicants = findViewById(R.id.rvApplicants);
        btnBack = findViewById(R.id.btnBack);

        if (rvApplicants != null) {
            rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    protected void initListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}