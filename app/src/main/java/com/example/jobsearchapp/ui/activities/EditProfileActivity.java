package com.example.jobsearchapp.ui.activities;

import android.widget.EditText;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {
    private EditText edtFullName, edtPhone, edtCompany, edtProfession, edtLocation;
    private EditText edtWebsite, edtCompanyDesc, edtLogo;
    private android.widget.LinearLayout layoutEmployerFields;
    private User currentUser;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected int getLayoutId() { return R.layout.candidate_activity_edit_profile; }

    @Override
    protected void initViews() {
        edtFullName = findViewById(R.id.edtEditFullName);
        edtPhone = findViewById(R.id.edtEditPhone);
        edtCompany = findViewById(R.id.edtEditCompany);
        edtProfession = findViewById(R.id.edtEditProfession);
        edtLocation = findViewById(R.id.edtEditLocation);
        
        edtWebsite = findViewById(R.id.edtEditWebsite);
        edtCompanyDesc = findViewById(R.id.edtEditCompanyDesc);
        edtLogo = findViewById(R.id.edtEditLogo);
        layoutEmployerFields = findViewById(R.id.layoutEmployerFields);

        db = FirebaseFirestore.getInstance();
        SessionManager sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();
        
        if ("employer".equalsIgnoreCase(sessionManager.getRole())) {
            layoutEmployerFields.setVisibility(android.view.View.VISIBLE);
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
                    }
                }
            })
            .addOnFailureListener(e -> showToast("Lỗi tải dữ liệu: " + e.getMessage()));
    }

    @Override
    protected void initListeners() {
        findViewById(R.id.ivBackEdit).setOnClickListener(v -> finish());
        
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
                updates.put("companyName", company);
                updates.put("profession", profession);
                updates.put("location", location);
                
                if (layoutEmployerFields.getVisibility() == android.view.View.VISIBLE) {
                    updates.put("companyWebsite", edtWebsite.getText().toString().trim());
                    updates.put("companyDescription", edtCompanyDesc.getText().toString().trim());
                    updates.put("avatarUrl", edtLogo.getText().toString().trim());
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
