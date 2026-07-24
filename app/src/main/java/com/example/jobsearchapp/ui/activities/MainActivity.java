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
import com.example.jobsearchapp.utils.SessionManager;
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

        SessionManager sessionManager = new SessionManager(this);
        if ("employer".equalsIgnoreCase(sessionManager.getRole())) {
            bottomNavigation.getMenu().findItem(R.id.nav_apps).setVisible(false);
            // Hiện tại đã cho phép nhà tuyển dụng dùng Search
            bottomNavigation.getMenu().findItem(R.id.nav_search).setVisible(true);
        }

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
                replaceFragment(new ProfileFragment());
                return true;
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