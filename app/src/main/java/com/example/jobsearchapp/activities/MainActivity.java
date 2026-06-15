package com.example.jobsearchapp.activities;

import android.content.Intent;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.adapters.JobAdapter;
import com.example.jobsearchapp.models.Job;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView rvJobsMain;
    private BottomNavigationView bottomNavigation;
    private ImageView ivNotification, ivLogo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        rvJobsMain = findViewById(R.id.rvJobsMain);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        ivNotification = findViewById(R.id.ivNotification);
        ivLogo = findViewById(R.id.ivLogo);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        List<Job> mockJobs = new ArrayList<>();
        mockJobs.add(new Job("1", "Lập trình viên Android", "15 - 25 triệu", "Hà Nội", "Kinh nghiệm 2 năm..."));
        mockJobs.add(new Job("2", "Designer UI/UX", "10 - 20 triệu", "TP. HCM", "Thành thạo Figma..."));
        mockJobs.add(new Job("3", "Backend Developer", "20 - 40 triệu", "Đà Nẵng", "Java, Spring Boot..."));
        mockJobs.add(new Job("4", "Kế toán tổng hợp", "8 - 12 triệu", "Hải Phòng", "Cẩn thận, trung thực..."));

        JobAdapter adapter = new JobAdapter(mockJobs);
        rvJobsMain.setLayoutManager(new LinearLayoutManager(this));
        rvJobsMain.setAdapter(adapter);
    }

    @Override
    protected void initListeners() {
        ivNotification.setOnClickListener(v -> showToast("Bạn không có thông báo mới"));

        // Click vào Logo để demo sang màn hình Nhà tuyển dụng (để bạn dễ test)
        ivLogo.setOnClickListener(v -> {
            startActivity(new Intent(this, EmployerActivity.class));
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showToast("Đang ở Trang chủ");
                return true;
            } else if (itemId == R.id.nav_search) {
                showToast("Tính năng Tìm kiếm đang phát triển");
                return true;
            } else if (itemId == R.id.nav_profile) {
                showToast("Mở Hồ sơ cá nhân");
                return true;
            }
            return false;
        });
    }
}