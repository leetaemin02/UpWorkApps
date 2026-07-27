package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.candidate.fragments.AppliedJobsFragment;
import com.example.jobsearchapp.ui.candidate.fragments.HomeFragment;
import com.example.jobsearchapp.ui.candidate.fragments.ProfileFragment;
import com.example.jobsearchapp.ui.candidate.fragments.SearchFragment;
import com.example.jobsearchapp.ui.employer.fragments.EmployerAppsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BaseActivity {

    private BottomNavigationView bottomNavigation;
    private TextView tvAppName;
    private ImageView ivProfile, ivNotification;
    private View viewNotifBadge;
    private com.google.firebase.firestore.ListenerRegistration notifListener;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        tvAppName = findViewById(R.id.tvAppName);
        ivProfile = findViewById(R.id.ivProfile);
        ivNotification = findViewById(R.id.ivNotification);
        viewNotifBadge = findViewById(R.id.viewNotifBadge);

        com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
        String role = sessionManager.getRole();
        String userId = sessionManager.getUserId();

        // Cập nhật nhãn Profile thành Login nếu chưa đăng nhập nhưng vẫn giữ icon người mới
        android.view.MenuItem profileItem = bottomNavigation.getMenu().findItem(R.id.nav_profile);
        if (userId.isEmpty()) {
            profileItem.setTitle("Login");
        } else {
            profileItem.setTitle("Profile");
        }
        profileItem.setIcon(R.drawable.ic_profile_custom); // Sử dụng icon người mới bạn đã gửi

        // Đảm bảo tab Apps luôn hiện
        bottomNavigation.getMenu().findItem(R.id.nav_apps).setVisible(true);

        bottomNavigation.getMenu().findItem(R.id.nav_search).setVisible(true);

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new HomeFragment());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProfileImage();
        setupNotificationBadge();
    }

    private void setupNotificationBadge() {
        com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
        String userId = sessionManager.getUserId();
        if (userId.isEmpty() || viewNotifBadge == null) {
            if (viewNotifBadge != null) viewNotifBadge.setVisibility(View.GONE);
            return;
        }

        if (notifListener != null) notifListener.remove();

        // Lắng nghe thông báo chưa đọc
        notifListener = FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("read", false)
                .addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        viewNotifBadge.setVisibility(View.VISIBLE);
                    } else {
                        viewNotifBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifListener.remove();
    }

    public void updateProfileImage() {
        if (ivProfile == null) return;
        
        com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
        String userId = sessionManager.getUserId();
        
        if (!userId.isEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String avatarUrl = doc.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_default_avatar)
                                .into(ivProfile);
                        } else {
                            ivProfile.setImageResource(R.drawable.ic_default_avatar);
                        }
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_default_avatar);
                    }
                })
                .addOnFailureListener(e -> ivProfile.setImageResource(R.drawable.ic_default_avatar));
        } else {
            ivProfile.setImageResource(R.drawable.ic_default_avatar);
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

        if (ivNotification != null) {
            ivNotification.setOnClickListener(v -> {
                com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
                if (sessionManager.getUserId().isEmpty()) {
                    startActivity(new Intent(this, AuthActivity.class));
                } else {
                    startActivity(new Intent(this, NotificationActivity.class));
                }
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
                com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(this);
                if ("employer".equalsIgnoreCase(sessionManager.getRole())) {
                    replaceFragment(new EmployerAppsFragment());
                } else {
                    replaceFragment(new AppliedJobsFragment());
                }
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