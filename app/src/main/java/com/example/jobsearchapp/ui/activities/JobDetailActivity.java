package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.Notification;
import com.example.jobsearchapp.data.models.SavedJob;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.FormatUtils;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class JobDetailActivity extends BaseActivity {

    private Job job;
    private Application application;
    private boolean isFinishing = false;
    private TextView tvTitle, tvSalary, tvLocation, tvDesc, tvReq, tvDetailCompany, tvExp, tvDeadline, tvFullAddress, tvAppliedStatus;
    private TextView tvDetailCategory, tvDetailQuantity, tvDetailBenefits, tvDetailBenefitsHeader;
    private android.widget.ImageView ivDetailLogo;
    private com.google.android.material.button.MaterialButton btnApplyNow, btnViewAppliedCv;

    private SessionManager sessionManager;
    private FirebaseFirestore db;
    private AppDatabase appDatabase;
    private boolean isSaved = false;
    private com.google.firebase.firestore.ListenerRegistration applicationListener;

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_activity_job_detail;
    }

    @Override
    protected void initViews() {
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvSalary = findViewById(R.id.tvDetailSalary);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDesc = findViewById(R.id.tvDetailDesc);
        tvReq = findViewById(R.id.tvDetailReq);
        tvDetailCompany = findViewById(R.id.tvDetailCompany);
        tvExp = findViewById(R.id.tvDetailExp);
        tvFullAddress = findViewById(R.id.tvDetailFullAddress);
        ivDetailLogo = findViewById(R.id.ivDetailLogo);
        tvAppliedStatus = findViewById(R.id.tvAppliedStatus);
        
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailQuantity = findViewById(R.id.tvDetailQuantity);
        tvDetailBenefits = findViewById(R.id.tvDetailBenefits);
        tvDetailBenefitsHeader = findViewById(R.id.tvDetailBenefitsHeader);

        btnApplyNow = findViewById(R.id.btnApplyNow);
        btnViewAppliedCv = findViewById(R.id.btnViewAppliedCv);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        appDatabase = AppDatabase.getInstance(this);

        // Nhận dữ liệu từ Intent
        try {
            job = (Job) getIntent().getSerializableExtra("JOB_DATA");
            application = (Application) getIntent().getSerializableExtra("APPLICATION_DATA");
        } catch (Exception e) {
            android.util.Log.e("JobDetail", "Error casting Intent data", e);
            job = null;
        }

        if (job != null) {
            if (application != null) {
                // CHẾ ĐỘ XEM ĐƠN ỨNG TUYỂN (Từ Notification / Apps tab)
                setupApplicationViewMode();
            } else {
                // CHẾ ĐỘ XEM CÔNG VIỆC (Từ Home / Search / Saved)
                setupJobViewMode();
            }
        } else {
            showToast("Lỗi: Không thể tải thông tin công việc");
            finish();
        }
    }

    private void setupJobViewMode() {
        displayJobInfo();
        checkIfJobIsSaved();
        
        // Luôn ẩn các thành phần của đơn ứng tuyển ở trang chi tiết công việc
        if (tvAppliedStatus != null) tvAppliedStatus.setVisibility(View.GONE);
        if (btnViewAppliedCv != null) btnViewAppliedCv.setVisibility(View.GONE);
        
        // Hiển thị nút nộp đơn dựa trên Role
        String role = sessionManager.getRole();
        if ("employer".equalsIgnoreCase(role)) {
            if (btnApplyNow != null) btnApplyNow.setVisibility(View.GONE);
        } else {
            // Hiển thị cho candidate và cả khi chưa đăng nhập
            if (btnApplyNow != null) btnApplyNow.setVisibility(View.VISIBLE);
        }
    }

    private void setupApplicationViewMode() {
        displayJobInfo();
        
        // Ẩn nút nộp đơn
        if (btnApplyNow != null) btnApplyNow.setVisibility(View.GONE);
        
        // Hiển thị trạng thái và nút xem CV
        if (tvAppliedStatus != null) tvAppliedStatus.setVisibility(View.VISIBLE);
        if (btnViewAppliedCv != null) btnViewAppliedCv.setVisibility(View.VISIBLE);
        
        displayApplicationInfo();
        setupApplicationRealtimeUpdate();
    }

    private void displayJobInfo() {
        if (job == null) return;

        if (tvTitle != null) tvTitle.setText(job.getTitle());
        if (tvSalary != null) tvSalary.setText(FormatUtils.formatSalaryRange(job.getSalaryMin(), job.getSalaryMax()));
        if (tvLocation != null) tvLocation.setText(job.getLocation());
        if (tvDesc != null) tvDesc.setText(job.getDescription());
        if (tvReq != null) tvReq.setText(job.getRequirements());
        if (tvDetailCompany != null) tvDetailCompany.setText(job.getCompanyName());
        if (tvExp != null) tvExp.setText(job.getExperienceRequired());
        if (tvFullAddress != null) tvFullAddress.setText(job.getLocation());

        if (tvDetailCategory != null) tvDetailCategory.setText(job.getCategory() != null ? job.getCategory() : "Khác");
        if (tvDetailQuantity != null) tvDetailQuantity.setText(String.format(java.util.Locale.getDefault(), "%02d Người", job.getQuantity()));

        if (tvDetailBenefits != null) {
            if (job.getBenefits() != null && !job.getBenefits().isEmpty()) {
                tvDetailBenefits.setText(job.getBenefits());
                tvDetailBenefits.setVisibility(View.VISIBLE);
                if (tvDetailBenefitsHeader != null) tvDetailBenefitsHeader.setVisibility(View.VISIBLE);
            } else {
                tvDetailBenefits.setVisibility(View.GONE);
                if (tvDetailBenefitsHeader != null) tvDetailBenefitsHeader.setVisibility(View.GONE);
            }
        }
    }

    private void setupApplicationRealtimeUpdate() {
        if (application == null || application.getId() == null) {
            displayApplicationInfo();
            return;
        }

        // Lắng nghe thay đổi trạng thái đơn ứng tuyển trong thời gian thực
        applicationListener = db.collection("applications").document(application.getId())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        android.util.Log.w("JobDetail", "Listen failed.", e);
                        displayApplicationInfo();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Application updatedApp = snapshot.toObject(Application.class);
                        if (updatedApp != null) {
                            updatedApp.setId(snapshot.getId());
                            this.application = updatedApp;
                            displayApplicationInfo();
                        }
                    } else {
                        displayApplicationInfo();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (applicationListener != null) {
            applicationListener.remove();
        }
    }

    private void displayApplicationInfo() {
        if (application == null || tvAppliedStatus == null) return;

        tvAppliedStatus.setVisibility(View.VISIBLE);
        String status = application.getStatus();
        if (status == null) status = "pending";
        
        String statusText;
        int statusColor;
        String bgColor;

        switch (status.toLowerCase()) {
            case "accepted":
            case "reviewed":
                statusText = "Trạng thái đơn: Phỏng vấn";
                statusColor = android.graphics.Color.parseColor("#198754"); // Green
                bgColor = "#E8F5E9";
                break;
            case "rejected":
                statusText = "Trạng thái đơn: Từ chối";
                statusColor = android.graphics.Color.parseColor("#DC3545"); // Red
                bgColor = "#FFEBEE";
                break;
            case "pending":
            default:
                statusText = "Trạng thái đơn: Đang xem xét";
                statusColor = android.graphics.Color.parseColor("#0D6EFD"); // Blue
                bgColor = "#E3F2FD";
                break;
        }

        tvAppliedStatus.setText(statusText);
        tvAppliedStatus.setTextColor(statusColor);
        tvAppliedStatus.setBackgroundColor(android.graphics.Color.parseColor(bgColor));

        // Hide Apply button and show View CV button
        if (btnApplyNow != null) btnApplyNow.setVisibility(View.GONE);
        if (btnViewAppliedCv != null) {
            btnViewAppliedCv.setVisibility(View.VISIBLE);
            btnViewAppliedCv.setOnClickListener(v -> {
                if (application.getCvUrl() != null && !application.getCvUrl().isEmpty()) {
                    try {
                        String url = application.getCvUrl();
                        if (!url.startsWith("http")) url = "https://" + url;
                        
                        android.content.Intent browserIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        showToast("Không thể mở CV: " + e.getMessage());
                    }
                } else {
                    showToast("Không tìm thấy CV đã nộp");
                }
            });
        }
    }

    private void checkIfJobIsSaved() {
        String userId = sessionManager.getUserId();
        if (userId.isEmpty() || job == null) return;

        db.collection("saved_jobs").document(userId + "_" + job.getId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    isSaved = documentSnapshot.exists();
                    updateHeartIcon();
                });
    }

    private void updateHeartIcon() {
        android.widget.ImageView ivHeart = findViewById(R.id.ivHeart);
        if (ivHeart != null) {
            ivHeart.setImageResource(isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    protected void initListeners() {
        if (isFinishing) return;

        View ivBack = findViewById(R.id.ivBack);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        if (btnApplyNow != null) {
            btnApplyNow.setOnClickListener(v -> {
                String userId = sessionManager.getUserId();
                if (userId.isEmpty()) {
                    // CHƯA ĐĂNG NHẬP -> Chuyển sang AuthActivity
                    startActivity(new Intent(this, AuthActivity.class));
                    return;
                }

                if (job == null) return;
                
                // Bước 1: Kiểm tra xem User đã có đơn ứng tuyển đang chờ (pending) cho công việc này chưa
                db.collection("applications")
                    .whereEqualTo("candidateId", userId)
                    .whereEqualTo("jobId", job.getId())
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            showToast("Bạn đã nộp đơn cho công việc này rồi, vui lòng chờ kết quả");
                        } else {
                            // Bước 2: Kiểm tra xem User đã có CV chưa
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String cvPath = documentSnapshot.getString("cvPath");
                                        String userName = documentSnapshot.getString("fullName");
                                        showApplyConfirmation(userId, cvPath, userName);
                                    }
                                });
                        }
                    })
                    .addOnFailureListener(e -> showToast("Lỗi kiểm tra đơn ứng tuyển: " + e.getMessage()));
            });
        }

        View ivHeart = findViewById(R.id.ivHeart);
        if (ivHeart != null) {
            ivHeart.setOnClickListener(v -> {
                String userId = sessionManager.getUserId();
                if (userId.isEmpty()) {
                    showToast("Vui lòng đăng nhập");
                    return;
                }

                if (job == null) return;

                if (!isSaved) {
                    SavedJob savedJob = new SavedJob(userId, job.getId());
                    db.collection("saved_jobs").document(userId + "_" + job.getId())
                            .set(savedJob)
                            .addOnSuccessListener(aVoid -> {
                                isSaved = true;
                                updateHeartIcon();
                                showToast("Đã lưu công việc");
                            });
                } else {
                    db.collection("saved_jobs").document(userId + "_" + job.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                isSaved = false;
                                updateHeartIcon();
                                showToast("Đã bỏ lưu");
                            });
                }
            });
        }
    }

    private void showApplyConfirmation(String userId, String cvPath, String userName) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_apply_confirm, null);
        
        TextView tvStatus = view.findViewById(R.id.tvApplyStatus);
        TextView tvCvName = view.findViewById(R.id.tvConfirmCvName);
        com.google.android.material.button.MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmApply);
        View btnCancel = view.findViewById(R.id.btnCancelApply);

        if (cvPath != null && !cvPath.isEmpty()) {
            tvStatus.setText("Xác nhận nộp hồ sơ?");
            tvCvName.setText("Sử dụng CV: My_CV.pdf");
            tvCvName.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_main));
            
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                submitApplication(userId, cvPath, userName);
            });
        } else {
            tvStatus.setText("Bạn chưa có CV!");
            tvCvName.setText("Vui lòng tải lên CV (PDF) tại Hồ sơ");
            tvCvName.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.error));
            btnConfirm.setText("Đến trang Hồ sơ");
            
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                finish(); 
            });
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(view);
        dialog.show();
    }

    private void submitApplication(String userId, String cvPath, String userName) {
        Application app = new Application(job.getId(), userId, job.getCompanyId(), job.getEmployerId());
        app.setJobTitle(job.getTitle());
        app.setCompanyName(job.getCompanyName());
        app.setLocation(job.getLocation());
        app.setCvUrl(cvPath);
        app.setCandidateName(userName);
        app.setStatus("pending");
        app.setAppliedAt(System.currentTimeMillis());

        db.collection("applications")
                .add(app)
                .addOnSuccessListener(documentReference -> {
                    // Tạo thông báo cho Ứng viên
                    Notification notif = new Notification(
                            userId,
                            "Nộp đơn thành công",
                            "Bạn đã nộp đơn thành công vào vị trí " + job.getTitle(),
                            "application",
                            job.getId(),
                            documentReference.getId()
                    );
                    db.collection("notifications").add(notif);

                    showToast("Đã gửi yêu cầu ứng tuyển thành công!");
                    // Không thay đổi giao diện sau khi nộp, giữ nguyên nút nộp hồ sơ
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }
}
