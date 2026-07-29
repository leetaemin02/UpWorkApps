package com.example.jobsearchapp.ui.employer.fragments;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.ui.activities.ManageJobsActivity;
import com.example.jobsearchapp.ui.activities.PostJobActivity;
import com.example.jobsearchapp.ui.activities.ViewApplicantsActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmployerAppsFragment extends BaseFragment {

    private MaterialCardView cardPostJob, cardManageJob, cardApplicants;
    private TextView tvWelcomeName;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_fragment_apps;
    }

    @Override
    protected void initViews(View view) {
        cardPostJob = view.findViewById(R.id.cardEmployerPostJob);
        cardManageJob = view.findViewById(R.id.cardEmployerManageJob);
        cardApplicants = view.findViewById(R.id.cardEmployerApplicants);
        tvWelcomeName = view.findViewById(R.id.tvEmployerAppsWelcome);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());
        loadUserInfo();
    }

    private void loadUserInfo() {
        String userId = sessionManager.getUserId();
        if (!userId.isEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String companyName = documentSnapshot.getString("companyName");
                        String displayName = (companyName != null && !companyName.isEmpty()) ? companyName : fullName;
                        tvWelcomeName.setText("Chào, " + displayName + "!");
                    }
                });
        }
    }

    @Override
    protected void initListeners() {
        cardPostJob.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PostJobActivity.class)));

        cardManageJob.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ManageJobsActivity.class)));

        cardApplicants.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ViewApplicantsActivity.class)));
    }
}
