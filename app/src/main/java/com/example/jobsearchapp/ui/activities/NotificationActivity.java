package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Applicant;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Notification;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.candidate.adapters.NotificationAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private ProgressBar pbNotifications;
    private View layoutNoNotif;
    private TextView tvReadAll;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_notifications;
    }

    @Override
    protected void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        pbNotifications = findViewById(R.id.pbNotifications);
        layoutNoNotif = findViewById(R.id.layoutNoNotif);
        tvReadAll = findViewById(R.id.tvReadAll);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notificationList, this);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        pbNotifications.setVisibility(View.VISIBLE);
        db.collection("notifications")
                .whereEqualTo("recipientId", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    pbNotifications.setVisibility(View.GONE);
                    if (e != null) {
                        showToast("Lỗi tải thông báo: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Notification notif = doc.toObject(Notification.class);
                            notif.setNotificationId(doc.getId());
                            notificationList.add(notif);
                        }
                        
                        // Sắp xếp thủ công theo thời gian
                        java.util.Collections.sort(notificationList, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                        
                        adapter.updateList(notificationList);
                        layoutNoNotif.setVisibility(notificationList.isEmpty() ? View.VISIBLE : View.GONE);
                        tvReadAll.setVisibility(notificationList.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // 1. Đánh dấu đã đọc
        if (!notification.isRead()) {
            db.collection("notifications").document(notification.getNotificationId()).update("read", true);
        }

        String role = sessionManager.getRole();

        // 2. Chuyển hướng thông minh dựa trên Role và Loại thông báo
        if ("employer".equalsIgnoreCase(role) && "application".equalsIgnoreCase(notification.getType())) {
            // TRƯỜNG HỢP: Nhà tuyển dụng nhấn vào "Ứng viên mới" -> Xem Chi tiết ứng viên
            navigateToApplicantDetail(notification);
        } else {
            // TRƯỜNG HỢP: Các loại khác (Đăng tin/Sửa tin của NTD, hoặc Thông báo của Ứng viên)
            navigateToJobDetail(notification);
        }
    }

    private void navigateToApplicantDetail(Notification notification) {
        if (notification.getApplicationId() == null) return;
        pbNotifications.setVisibility(View.VISIBLE);

        db.collection("applications").document(notification.getApplicationId()).get()
            .addOnSuccessListener(appDoc -> {
                if (appDoc.exists()) {
                    Application app = appDoc.toObject(Application.class);
                    if (app == null) { pbNotifications.setVisibility(View.GONE); return; }
                    
                    db.collection("users").document(app.getCandidateId()).get()
                        .addOnSuccessListener(userDoc -> {
                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);
                                if (user != null) {
                                    Applicant applicant = new Applicant();
                                    applicant.setApplicationId(appDoc.getId());
                                    applicant.setId(userDoc.getId());
                                    applicant.setName(user.getFullName());
                                    applicant.setEmail(user.getEmail());
                                    applicant.setCvUrl(app.getCvUrl());
                                    applicant.setAvatarUrl(app.getCandidateAvatarUrl());
                                    applicant.setJobTitle(app.getJobTitle());
                                    applicant.setJobId(app.getJobId());
                                    applicant.setStatus(app.getStatus());

                                    Intent intent = new Intent(this, ApplicantDetailActivity.class);
                                    intent.putExtra("APPLICANT_DATA", applicant);
                                    startActivity(intent);
                                }
                            }
                            pbNotifications.setVisibility(View.GONE);
                        });
                } else {
                    pbNotifications.setVisibility(View.GONE);
                    showToast("Đơn ứng tuyển này không còn tồn tại");
                }
            });
    }

    private void navigateToJobDetail(Notification notification) {
        if (notification.getJobId() == null) return;
        pbNotifications.setVisibility(View.VISIBLE);

        db.collection("jobs").document(notification.getJobId()).get()
            .addOnSuccessListener(jobDoc -> {
                if (jobDoc.exists()) {
                    Job job = jobDoc.toObject(Job.class);
                    if (job != null) job.setId(jobDoc.getId());

                    if (notification.getApplicationId() != null) {
                        // Tải thêm Đơn ứng tuyển nếu có (Dành cho Ứng viên xem kết quả)
                        db.collection("applications").document(notification.getApplicationId()).get()
                            .addOnSuccessListener(appDoc -> {
                                pbNotifications.setVisibility(View.GONE);
                                Intent intent = new Intent(this, JobDetailActivity.class);
                                intent.putExtra("JOB_DATA", job);
                                if (appDoc.exists()) {
                                    Application app = appDoc.toObject(Application.class);
                                    if (app != null) app.setId(appDoc.getId());
                                    intent.putExtra("APPLICATION_DATA", app);
                                }
                                startActivity(intent);
                            });
                    } else {
                        // Chỉ xem Chi tiết công việc (Dành cho NTD xem tin vừa đăng/sửa)
                        pbNotifications.setVisibility(View.GONE);
                        Intent intent = new Intent(this, JobDetailActivity.class);
                        intent.putExtra("JOB_DATA", job);
                        startActivity(intent);
                    }
                } else {
                    pbNotifications.setVisibility(View.GONE);
                    showToast("Công việc này không còn tồn tại");
                }
            });
    }

    private void markAllAsRead() {
        WriteBatch batch = db.batch();
        boolean hasUnread = false;
        for (Notification n : notificationList) {
            if (!n.isRead()) {
                batch.update(db.collection("notifications").document(n.getNotificationId()), "read", true);
                hasUnread = true;
            }
        }

        if (hasUnread) {
            batch.commit().addOnSuccessListener(aVoid -> showToast("Đã đánh dấu tất cả là đã đọc"));
        }
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBackNotif).setOnClickListener(v -> finish());
        tvReadAll.setOnClickListener(v -> markAllAsRead());
    }
}
