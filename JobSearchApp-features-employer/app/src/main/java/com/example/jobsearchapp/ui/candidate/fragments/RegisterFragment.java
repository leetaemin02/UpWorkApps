package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import android.widget.EditText;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends BaseFragment {
    private EditText edtEmail, edtPassword, edtFullName, edtPhone;
    private MaterialButtonToggleGroup toggleGroupRole;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() { return R.layout.fragment_register; }

    @Override
    protected void initViews(View view) {
        edtFullName = view.findViewById(R.id.edtFullName);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        toggleGroupRole = view.findViewById(R.id.toggleGroupRole);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void initListeners() {
        if (getView() == null) return;

        getView().findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            
            if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showToast("Vui lòng điền đầy đủ thông tin");
                return;
            }

            String selectedRole = "candidate";
            if (toggleGroupRole.getCheckedButtonId() == R.id.btnRoleEmployer) {
                selectedRole = "employer";
            }
            
            final String role = selectedRole;

            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("userId", uid);
                        userMap.put("fullName", fullName);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("role", role);
                        userMap.put("createdAt", System.currentTimeMillis());
                        userMap.put("status", "active");
                        
                        db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                showToast("Đăng ký thành công! Hãy đăng nhập.");
                                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                    getParentFragmentManager().popBackStack();
                                }
                            })
                            .addOnFailureListener(e -> showToast("Lỗi lưu thông tin: " + e.getMessage()));
                    } else {
                        showToast("Lỗi đăng ký: " + task.getException().getMessage());
                    }
                });
        });

        getView().findViewById(R.id.tvToLogin).setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
    }
}
