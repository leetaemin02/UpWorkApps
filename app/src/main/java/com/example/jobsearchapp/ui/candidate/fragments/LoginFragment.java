package com.example.jobsearchapp.ui.candidate.fragments;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.activities.MainActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends BaseFragment {
    private EditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() { return R.layout.fragment_login; }

    @Override
    protected void initViews(View view) {
        edtEmail = view.findViewById(R.id.edtEmailLogin);
        edtPassword = view.findViewById(R.id.edtPasswordLogin);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void initListeners() {
        if (getView() == null) return;

        getView().findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                showToast("Vui lòng nhập email và mật khẩu");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        fetchUserAndNavigate(uid);
                    } else {
                        showToast("Sai email hoặc mật khẩu hoặc lỗi hệ thống");
                    }
                });
        });

        getView().findViewById(R.id.tvToRegister).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.auth_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getView().findViewById(R.id.btnBackToGuest).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }

    private void fetchUserAndNavigate(String uid) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String role = documentSnapshot.getString("role");
                    new SessionManager(getContext()).saveSession(uid, role);

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                } else {
                    showToast("Không tìm thấy dữ liệu người dùng");
                }
            })
            .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }
}
