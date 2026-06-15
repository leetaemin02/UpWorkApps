package com.example.jobsearchapp.activities;

import android.widget.Button;
import android.widget.EditText;
import com.example.jobsearchapp.R;

public class PostJobActivity extends BaseActivity {

    private EditText edtJobName, edtSalary, edtLocation;
    private Button btnSave;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_job;
    }

    @Override
    protected void initViews() {
        edtJobName = findViewById(R.id.edtJobName);
        edtSalary = findViewById(R.id.edtSalary);
        edtLocation = findViewById(R.id.edtLocation);
        btnSave = findViewById(R.id.btnSave);
    }

    @Override
    protected void initListeners() {
        btnSave.setOnClickListener(v -> {
            String jobTitle = edtJobName.getText().toString();
            if (jobTitle.isEmpty()) {
                showToast("Vui lòng nhập tên công việc");
                return;
            }
            showToast("Đăng tin thành công: " + jobTitle);
            finish();
        });
    }
}