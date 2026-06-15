package com.example.jobsearchapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity chứa các hàm tiện ích dùng chung cho toàn bộ Activity trong app.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initViews();
        initListeners();
    }

    /**
     * Trả về ID của layout (ví dụ: R.layout.activity_main)
     */
    protected abstract int getLayoutId();

    /**
     * Khởi tạo các View (findViewById)
     */
    protected abstract void initViews();

    /**
     * Thiết lập các sự kiện click, v.v.
     */
    protected abstract void initListeners();

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}