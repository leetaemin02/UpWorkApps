package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.FirebaseStorageHelper;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {
    private EditText edtFullName, edtPhone, edtCompany, edtProfession, edtLocation;
    private EditText edtWebsite, edtCompanyDesc, edtLogo;
    private TextView tvTitle;
    private android.widget.LinearLayout layoutEmployerFields;
    private ImageView ivAvatar;
    private TextView tvEditAvatar;
    private User currentUser;
    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<String> avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleSelectedAvatar(uri); }
    );

    @Override
    protected int getLayoutId() { return R.layout.candidate_activity_edit_profile; }

    @Override
    protected void initViews() {
        tvTitle = findViewById(R.id.tvEditProfileTitle);
        edtFullName = findViewById(R.id.edtEditFullName);
        edtPhone = findViewById(R.id.edtEditPhone);
        edtCompany = findViewById(R.id.edtEditCompany);
        edtProfession = findViewById(R.id.edtEditProfession);
        edtLocation = findViewById(R.id.edtEditLocation);
        
        edtWebsite = findViewById(R.id.edtEditWebsite);
        edtCompanyDesc = findViewById(R.id.edtEditCompanyDesc);
        edtLogo = findViewById(R.id.edtEditLogo);
        layoutEmployerFields = findViewById(R.id.layoutEmployerFields);
        ivAvatar = findViewById(R.id.ivEditProfileAvatar);
        tvEditAvatar = findViewById(R.id.tvEditAvatarLabel);

        db = FirebaseFirestore.getInstance();
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        
        if ("employer".equalsIgnoreCase(sessionManager.getRole())) {
            if (tvTitle != null) tvTitle.setText("Chỉnh sửa thông tin công ty");
            if (tvEditAvatar != null) tvEditAvatar.setText("Đổi logo công ty");
            
            layoutEmployerFields.setVisibility(android.view.View.VISIBLE);
            
            // Ẩn các trường dư thừa cho Nhà tuyển dụng
            if (edtProfession != null) edtProfession.setVisibility(View.GONE);
            View tvLabelProfession = findViewById(R.id.tvLabelProfession);
            if (tvLabelProfession != null) tvLabelProfession.setVisibility(View.GONE);

            if (edtCompany != null) edtCompany.setVisibility(View.GONE);
            View tvLabelCompany = findViewById(R.id.tvLabelCompany);
            if (tvLabelCompany != null) tvLabelCompany.setVisibility(View.GONE);

            if (edtLogo != null) edtLogo.setVisibility(View.GONE);
            View tvLabelLogo = findViewById(R.id.tvLabelLogo);
            if (tvLabelLogo != null) tvLabelLogo.setVisibility(View.GONE);

            if (edtWebsite != null) edtWebsite.setVisibility(View.GONE);
            View tvLabelWebsite = findViewById(R.id.tvLabelWebsite);
            if (tvLabelWebsite != null) tvLabelWebsite.setVisibility(View.GONE);
            
            // Cập nhật nhãn "Họ và tên" thành "Tên công ty"
            View tvLabelFullName = findViewById(R.id.tvLabelFullName);
            if (tvLabelFullName instanceof TextView) {
                ((TextView) tvLabelFullName).setText("Tên công ty");
            }
            edtFullName.setHint("Nhập tên công ty");
        }

        loadUserData();
    }

    private void loadUserData() {
        if (userId == null || userId.isEmpty()) return;
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        edtFullName.setText(currentUser.getFullName());
                        edtPhone.setText(currentUser.getPhone());
                        edtCompany.setText(currentUser.getCompanyName());
                        edtProfession.setText(currentUser.getProfession());
                        edtLocation.setText(currentUser.getLocation());
                        
                        if (edtWebsite != null) edtWebsite.setText(currentUser.getCompanyWebsite());
                        if (edtCompanyDesc != null) edtCompanyDesc.setText(currentUser.getCompanyDescription());
                        if (edtLogo != null) edtLogo.setText(currentUser.getAvatarUrl());

                        displayAvatar(currentUser.getAvatarUrl());
                    }
                }
            })
            .addOnFailureListener(e -> showToast("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    private void displayAvatar(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void handleSelectedAvatar(Uri uri) {
        showToast("Đang tải ảnh đại diện...");
        FirebaseStorageHelper storageHelper = new FirebaseStorageHelper();
        storageHelper.uploadAvatar(this, uri, userId, new FirebaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                db.collection("users").document(userId)
                    .update("avatarUrl", downloadUrl)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Cập nhật ảnh thành công");
                        displayAvatar(downloadUrl);
                    });
            }

            @Override
            public void onFailure(Exception e) {
                showToast("Lỗi tải ảnh: " + e.getMessage());
            }
        });
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBackEdit).setOnClickListener(v -> finish());

        if (ivAvatar != null) {
            ivAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }
        if (tvEditAvatar != null) {
            tvEditAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }
        
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> {
            if (userId != null && !userId.isEmpty()) {
                String fullName = edtFullName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String company = edtEditCompanyVisible() ? edtCompany.getText().toString().trim() : "";
                String profession = edtProfession.getText().toString().trim();
                String location = edtLocation.getText().toString().trim();

                if (fullName.isEmpty()) {
                    showToast("Họ tên không được để trống");
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("fullName", fullName);
                updates.put("phone", phone);
                updates.put("location", location);
                
                if (layoutEmployerFields.getVisibility() == android.view.View.VISIBLE) {
                    updates.put("companyName", fullName); // Đồng bộ companyName với fullName cho NTD
                    updates.put("companyDescription", edtCompanyDesc.getText().toString().trim());
                    // Ẩn Website/Logo URL text field theo yêu cầu tinh gọn
                } else {
                    updates.put("profession", profession);
                    updates.put("companyName", company);
                }
                
                db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Cập nhật thành công");
                        finish();
                    })
                    .addOnFailureListener(e -> showToast("Lỗi cập nhật: " + e.getMessage()));
            } else {
                showToast("Lỗi: Không tìm thấy ID người dùng");
            }
        });
    }

    private boolean edtEditCompanyVisible() {
        return edtCompany.getVisibility() == android.view.View.VISIBLE;
    }
}
