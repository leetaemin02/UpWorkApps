package com.example.jobsearchapp.ui.candidate.fragments;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.activities.AuthActivity;
import com.example.jobsearchapp.ui.activities.EditProfileActivity;
import com.example.jobsearchapp.ui.activities.MainActivity;
import com.example.jobsearchapp.ui.activities.ManageJobsActivity;
import com.example.jobsearchapp.ui.activities.PostJobActivity;
import com.example.jobsearchapp.ui.activities.ViewApplicantsActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.utils.FirebaseStorageHelper;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends BaseFragment {
    private TextView tvName, tvEmail, tvLocation, tvCvName, tvEditAvatar;
    private TextView tvEmployerCompanyName, tvEmployerEmail, tvEmployerPhone, tvEmployerDescription, tvEmployerAddress, tvEditEmployerAvatar;
    private LinearLayout layoutLoggedIn, layoutGuest, layoutCvItem, layoutEmployerProfile;
    private android.widget.ImageView ivProfileAvatar, ivEmployerAvatar;
    private ChipGroup cgProfileSkills;
    private SessionManager sessionManager;
    private User currentUser;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> cvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleSelectedCV(uri); }
    );

    private final ActivityResultLauncher<String> avatarPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleSelectedAvatar(uri); }
    );

    @Override
    protected int getLayoutId() { return R.layout.candidate_fragment_profile; }

    @Override
    protected void initViews(View view) {
        // Candidate views
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvLocation = view.findViewById(R.id.tvProfileLocation);
        tvCvName = view.findViewById(R.id.tvCvName);
        tvEditAvatar = view.findViewById(R.id.tvEditAvatar);
        layoutCvItem = view.findViewById(R.id.layout_cv_item);
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        cgProfileSkills = view.findViewById(R.id.cgProfileSkills);

        // Employer views
        layoutEmployerProfile = view.findViewById(R.id.layout_employer_profile);
        ivEmployerAvatar = view.findViewById(R.id.ivEmployerAvatar);
        tvEmployerCompanyName = view.findViewById(R.id.tvEmployerCompanyName);
        tvEditEmployerAvatar = view.findViewById(R.id.tvEditEmployerAvatar);
        tvEmployerEmail = view.findViewById(R.id.tvEmployerEmail);
        tvEmployerPhone = view.findViewById(R.id.tvEmployerPhone);
        tvEmployerDescription = view.findViewById(R.id.tvEmployerDescription);
        tvEmployerAddress = view.findViewById(R.id.tvEmployerAddress);

        layoutGuest = view.findViewById(R.id.layout_guest);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        String userId = sessionManager.getUserId();
        String role = sessionManager.getRole();

        if (userId.isEmpty()) {
            layoutGuest.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);
            layoutEmployerProfile.setVisibility(View.GONE);
        } else {
            layoutGuest.setVisibility(View.GONE);
            if ("employer".equalsIgnoreCase(role)) {
                layoutLoggedIn.setVisibility(View.GONE);
                layoutEmployerProfile.setVisibility(View.VISIBLE);
            } else {
                layoutLoggedIn.setVisibility(View.VISIBLE);
                layoutEmployerProfile.setVisibility(View.GONE);
            }
            loadUserData(userId, role);
        }
    }

    private void loadUserData(String userId, String role) {
        if (getContext() == null) return;
        db.collection("users").document(userId)
            .addSnapshotListener((documentSnapshot, e) -> {
                if (e != null || documentSnapshot == null || !documentSnapshot.exists()) return;
                currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    currentUser.setId(documentSnapshot.getId());
                    if ("employer".equalsIgnoreCase(role)) {
                        displayEmployerData();
                    } else {
                        displayCandidateData();
                    }
                }
            });
    }

    private void displayEmployerData() {
        tvEmployerCompanyName.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "Tên Công Ty");
        tvEmployerEmail.setText(currentUser.getEmail());
        tvEmployerPhone.setText("Số điện thoại: " + (currentUser.getPhone() != null ? currentUser.getPhone() : "Chưa cập nhật"));
        tvEmployerDescription.setText(currentUser.getCompanyDescription() != null ? currentUser.getCompanyDescription() : "Chưa có mô tả công ty");
        tvEmployerAddress.setText(currentUser.getLocation() != null ? currentUser.getLocation() : "Chưa cập nhật địa chỉ");

        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(ivEmployerAvatar);
        } else {
            ivEmployerAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void displayCandidateData() {
        tvName.setText(currentUser.getFullName());
        tvEmail.setText("Email: " + currentUser.getEmail());
        tvLocation.setText("Địa điểm: " + (currentUser.getLocation() != null ? currentUser.getLocation() : "Chưa cập nhật"));

        if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                .load(currentUser.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(ivProfileAvatar);
        } else {
            ivProfileAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        if (currentUser.getCvPath() != null && !currentUser.getCvPath().isEmpty()) {
            layoutCvItem.setVisibility(android.view.View.VISIBLE);
            tvCvName.setText("My_CV.pdf");
        } else {
            layoutCvItem.setVisibility(android.view.View.GONE);
        }
        loadSkills(currentUser.getSkills());
    }

    private void deleteCV() {
        if (currentUser == null) return;
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Xóa CV")
            .setMessage("Bạn có chắc chắn muốn xóa CV này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                db.collection("users").document(currentUser.getId())
                    .update("cvPath", "")
                    .addOnSuccessListener(aVoid -> {
                        showToast("Đã xóa CV");
                        currentUser.setCvPath("");
                        layoutCvItem.setVisibility(View.GONE);
                    });
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void loadSkills(String skillsStr) {
        cgProfileSkills.removeAllViews();
        if (skillsStr != null && !skillsStr.isEmpty()) {
            for (String skill : skillsStr.split(",")) {
                if (skill.trim().isEmpty()) continue;
                Chip chip = new Chip(requireContext());
                chip.setText(skill.trim());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> removeSkill(skill.trim()));
                cgProfileSkills.addView(chip);
            }
        }
    }

    private void handleSelectedCV(Uri uri) {
        if (currentUser == null) return;
        
        showToast("Đang tải CV lên...");
        FirebaseStorageHelper storageHelper = new FirebaseStorageHelper();
        storageHelper.uploadCV(uri, currentUser.getId(), new FirebaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("cvPath", downloadUrl);

                db.collection("users").document(currentUser.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Đã tải lên CV thành công! AI đang tự động trích xuất kỹ năng & thông tin...");
                        loadUserData(currentUser.getId(), sessionManager.getRole());
                    });
            }

            @Override
            public void onFailure(Exception e) {
                showToast("Lỗi tải CV: " + e.getMessage());
            }
        });
    }

    private void handleSelectedAvatar(Uri uri) {
        if (currentUser == null) return;

        showToast("Đang tải ảnh đại diện...");
        FirebaseStorageHelper storageHelper = new FirebaseStorageHelper();
        storageHelper.uploadAvatar(requireContext(), uri, currentUser.getId(), new FirebaseStorageHelper.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("avatarUrl", downloadUrl);

                db.collection("users").document(currentUser.getId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Cập nhật ảnh đại diện thành công");
                        loadUserData(currentUser.getId(), sessionManager.getRole());
                        
                        // Cập nhật cả ở MainActivity
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).updateProfileImage();
                        }
                    });
            }

            @Override
            public void onFailure(Exception e) {
                showToast("Lỗi tải ảnh: " + e.getMessage());
            }
        });
    }

    private void addSkill(String skill) {
        if (currentUser == null) return;
        String current = currentUser.getSkills();
        String updated = (current == null || current.isEmpty()) ? skill : current + "," + skill;

        db.collection("users").document(currentUser.getId())
                .update("skills", updated)
                .addOnSuccessListener(aVoid -> loadUserData(currentUser.getId(), sessionManager.getRole()));
    }

    private void removeSkill(String skill) {
        if (currentUser == null || currentUser.getSkills() == null) return;
        StringBuilder sb = new StringBuilder();
        for (String s : currentUser.getSkills().split(",")) {
            if (!s.trim().equals(skill)) {
                if (sb.length() > 0) sb.append(",");
                sb.append(s.trim());
            }
        }
        db.collection("users").document(currentUser.getId())
                .update("skills", sb.toString())
                .addOnSuccessListener(aVoid -> loadUserData(currentUser.getId(), sessionManager.getRole()));
    }

    @Override
    protected void initListeners() {
        if (getView() == null) return;

        getView().findViewById(R.id.btnGoToAuth).setOnClickListener(v -> startActivity(new Intent(getActivity(), AuthActivity.class)));
        getView().findViewById(R.id.btnUploadCV).setOnClickListener(v -> cvPickerLauncher.launch("application/pdf"));

        if (ivProfileAvatar != null) {
            ivProfileAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }
        if (tvEditAvatar != null) {
            tvEditAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }

        View ivDeleteCv = getView().findViewById(R.id.ivDeleteCv);
        if (ivDeleteCv != null) {
            ivDeleteCv.setOnClickListener(v -> deleteCV());
        }

        layoutCvItem.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getCvPath() != null && !currentUser.getCvPath().isEmpty()) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUser.getCvPath()));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    showToast("Không thể mở CV: " + e.getMessage());
                }
            }
        });

        getView().findViewById(R.id.btnEditProfile).setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        getView().findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        getView().findViewById(R.id.ivAddSkill).setOnClickListener(v -> showSkillDialog());

        // Employer listeners
        if (ivEmployerAvatar != null) {
            ivEmployerAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }
        if (tvEditEmployerAvatar != null) {
            tvEditEmployerAvatar.setOnClickListener(v -> avatarPickerLauncher.launch("image/*"));
        }
        getView().findViewById(R.id.btnEditEmployerProfile).setOnClickListener(v -> 
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        getView().findViewById(R.id.btnLogoutEmployer).setOnClickListener(v -> logout());

        View cardSavedJobs = getView().findViewById(R.id.cardSavedJobs);
        if (cardSavedJobs != null) {
            cardSavedJobs.setOnClickListener(v -> startActivity(new Intent(getActivity(), com.example.jobsearchapp.ui.activities.SavedJobsActivity.class)));
        }
    }

    private void showSkillDialog() {
        String[] skillList = {
                "Java", "Android", "Firebase", "Git", "SQL", "Kotlin",
                "UI/UX", "Figma", "HTML", "CSS", "JavaScript", "PHP",
                "Laravel", "ReactJS", "NodeJS", "Python"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn kỹ năng")
                .setItems(skillList, (dialog, which) -> addSkill(skillList[which]))
                .show();
    }

    private void logout() {
        sessionManager.logout();
        if (getActivity() != null) {
            // Khởi động lại MainActivity để xóa sạch trạng thái cũ và hiển thị đầy đủ 4 tab cho ứng viên/khách
            Intent intent = new Intent(getActivity(), com.example.jobsearchapp.ui.activities.MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
