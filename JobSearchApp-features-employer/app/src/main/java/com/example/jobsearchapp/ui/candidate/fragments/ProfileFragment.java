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
import com.example.jobsearchapp.ui.activities.ManageJobsActivity;
import com.example.jobsearchapp.ui.activities.PostJobActivity;
import com.example.jobsearchapp.ui.activities.ViewApplicantsActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends BaseFragment {
    private TextView tvName, tvEmail, tvProfession, tvLocation, tvCvName, tvEmployerWelcome;
    private LinearLayout layoutLoggedIn, layoutGuest, layoutCvItem, layoutEmployerDashboard;
    private ChipGroup cgProfileSkills;
    private SessionManager sessionManager;
    private User currentUser;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> cvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleSelectedCV(uri); }
    );

    @Override
    protected int getLayoutId() { return R.layout.candidate_fragment_profile; }

    @Override
    protected void initViews(View view) {
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfession = view.findViewById(R.id.tvProfileProfession);
        tvLocation = view.findViewById(R.id.tvProfileLocation);
        tvCvName = view.findViewById(R.id.tvCvName);
        layoutLoggedIn = view.findViewById(R.id.layout_logged_in);
        layoutGuest = view.findViewById(R.id.layout_guest);
        layoutCvItem = view.findViewById(R.id.layout_cv_item);
        cgProfileSkills = view.findViewById(R.id.cgProfileSkills);

        layoutEmployerDashboard = view.findViewById(R.id.layout_employer_dashboard);
        tvEmployerWelcome = view.findViewById(R.id.tvEmployerWelcome);

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
            layoutEmployerDashboard.setVisibility(View.GONE);
        } else {
            layoutGuest.setVisibility(View.GONE);
            loadUserData(userId, role);
        }
    }

    private void loadUserData(String userId, String role) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        currentUser.setId(documentSnapshot.getId());
                        if ("employer".equalsIgnoreCase(role)) {
                            layoutLoggedIn.setVisibility(View.GONE);
                            layoutEmployerDashboard.setVisibility(View.VISIBLE);
                            tvEmployerWelcome.setText("Chào, " + currentUser.getFullName() + "!");
                        } else {
                            layoutLoggedIn.setVisibility(View.VISIBLE);
                            layoutEmployerDashboard.setVisibility(View.GONE);
                            displayCandidateData();
                        }
                    }
                }
            });
    }

    private void displayCandidateData() {
        tvName.setText(currentUser.getFullName());
        tvEmail.setText(currentUser.getEmail());
        tvProfession.setText(currentUser.getProfession());
        tvLocation.setText(currentUser.getLocation());

        if (currentUser.getCvPath() != null && !currentUser.getCvPath().isEmpty()) {
            layoutCvItem.setVisibility(View.VISIBLE);
            tvCvName.setText(currentUser.getCvPath());
        } else {
            layoutCvItem.setVisibility(View.GONE);
        }
        loadSkills(currentUser.getSkills());
    }

    private void loadSkills(String skillsStr) {
        cgProfileSkills.removeAllViews();
        if (skillsStr != null && !skillsStr.isEmpty()) {
            for (String skill : skillsStr.split(",")) {
                if (skill.trim().isEmpty()) continue;
                Chip chip = new Chip(getContext());
                chip.setText(skill.trim());
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(v -> removeSkill(skill.trim()));
                cgProfileSkills.addView(chip);
            }
        }
    }

    private void handleSelectedCV(Uri uri) {
        if (currentUser == null) return;
        String fileName = uri.getLastPathSegment();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("cvPath", fileName);
        
        db.collection("users").document(currentUser.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                showToast("Đã cập nhật CV thành công");
                loadUserData(currentUser.getId(), sessionManager.getRole());
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
        getView().findViewById(R.id.btnUploadCV).setOnClickListener(v -> cvPickerLauncher.launch("*/*"));
        getView().findViewById(R.id.btnEditProfile).setOnClickListener(v -> startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        getView().findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
        getView().findViewById(R.id.ivAddSkill).setOnClickListener(v -> {
            EditText input = new EditText(getContext());
            new AlertDialog.Builder(getContext()).setTitle("Thêm kỹ năng").setView(input)
                    .setPositiveButton("Thêm", (d, w) -> {
                        String s = input.getText().toString().trim();
                        if (!s.isEmpty()) addSkill(s);
                    }).setNegativeButton("Hủy", null).show();
        });

        getView().findViewById(R.id.cardPostJob).setOnClickListener(v -> startActivity(new Intent(getActivity(), PostJobActivity.class)));
        getView().findViewById(R.id.cardManageJob).setOnClickListener(v -> startActivity(new Intent(getActivity(), ManageJobsActivity.class)));
        getView().findViewById(R.id.cardApplicants).setOnClickListener(v -> startActivity(new Intent(getActivity(), ViewApplicantsActivity.class)));
        getView().findViewById(R.id.btnLogoutEmployer).setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.logout();
        checkLoginStatus();
    }
}
