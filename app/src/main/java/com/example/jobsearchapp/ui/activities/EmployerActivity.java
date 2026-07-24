package com.example.jobsearchapp.ui.activities;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmployerActivity extends BaseActivity {

    private MaterialCardView cardPostJob, cardManageJob, cardApplicants;
    private ImageView ivLogout;
    private TextView tvWelcomeName;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity;
    }

    @Override
    protected void initViews() {
        cardPostJob = findViewById(R.id.cardPostJob);
        cardManageJob = findViewById(R.id.cardManageJob);
        cardApplicants = findViewById(R.id.cardApplicants);
        ivLogout = findViewById(R.id.ivLogout);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        loadUserInfo();
    }

    private void loadUserInfo() {
        String userId = sessionManager.getUserId();
        if (!userId.isEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        tvWelcomeName.setText("Chào, " + fullName + "!");
                    }
                });
        }
    }

    @Override
    protected void initListeners() {
        cardPostJob.setOnClickListener(v ->
                startActivity(new Intent(this, PostJobActivity.class)));

        cardManageJob.setOnClickListener(v ->
                startActivity(new Intent(this, ManageJobsActivity.class)));

        cardApplicants.setOnClickListener(v ->
                startActivity(new Intent(this, ViewApplicantsActivity.class)));

        ivLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
