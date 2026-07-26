package com.example.jobsearchapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.candidate.fragments.AppliedJobsFragment;
import com.example.jobsearchapp.ui.candidate.fragments.HomeFragment;
import com.example.jobsearchapp.ui.candidate.fragments.ProfileFragment;
import com.example.jobsearchapp.ui.candidate.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNavigation;
    private TextView tvAppName;
    private View ivProfile;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvAppName = findViewById(R.id.tvAppName);
        ivProfile = findViewById(R.id.ivProfile);

        com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
        String role = sessionManager.getRole();
        String userId = sessionManager.getUserId();

        // Cập nhật nhãn Profile thành Login nếu chưa đăng nhập nhưng vẫn giữ icon người
        android.view.MenuItem profileItem = bottomNavigation.getMenu().findItem(R.id.nav_profile);
        if (userId.isEmpty()) {
            profileItem.setTitle("Login");
        } else {
            profileItem.setTitle("Profile");
        }
        profileItem.setIcon(android.R.drawable.ic_menu_myplaces); // Luôn giữ icon người (hoặc icon profile mặc định)

        // Ẩn tab Apps nếu là Nhà tuyển dụng (Employer)
        if ("employer".equalsIgnoreCase(role)) {
            bottomNavigation.getMenu().findItem(R.id.nav_apps).setVisible(false);
        } else {
            bottomNavigation.getMenu().findItem(R.id.nav_apps).setVisible(true);
        }

        bottomNavigation.getMenu().findItem(R.id.nav_search).setVisible(true);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new HomeFragment());
        }
    }

    @Override
    protected void initListeners() {
        // Nhấn vào chữ CareerLaunch -> Về trang chủ
        if (tvAppName != null) {
            tvAppName.setOnClickListener(v -> {
                bottomNavigation.setSelectedItemId(R.id.nav_home);
            });
        }

        // Nhấn vào ảnh đại diện góc phải -> Sang trang hồ sơ
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                bottomNavigation.setSelectedItemId(R.id.nav_profile);
            });
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                replaceFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_search) {
                replaceFragment(new SearchFragment());
                return true;
            } else if (itemId == R.id.nav_apps) {
                replaceFragment(new AppliedJobsFragment());
                return true;
            } else if (itemId == R.id.nav_profile) {
                com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
                if (sessionManager.getUserId().isEmpty()) {
                    // Nếu chưa đăng nhập, chuyển hướng sang AuthActivity
                    android.content.Intent intent = new android.content.Intent(this, AuthActivity.class);
                    startActivity(intent);
                    return false; // Không chọn item này trên bottom nav vì ta chuyển activity
                } else {
                    replaceFragment(new ProfileFragment());
                    return true;
                }
            }
            return false;
        });
    }

    public void navigateToSearch(String query) {
        SearchFragment searchFragment = new SearchFragment();
        if (query != null) {
            Bundle args = new Bundle();
            args.putString("SEARCH_QUERY", query);
            searchFragment.setArguments(args);
        }
        bottomNavigation.setSelectedItemId(R.id.nav_search);
        replaceFragment(searchFragment);
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}