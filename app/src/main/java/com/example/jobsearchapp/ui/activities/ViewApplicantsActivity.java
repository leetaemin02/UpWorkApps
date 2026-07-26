package com.example.jobsearchapp.ui.activities;

import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Applicant;
import com.example.jobsearchapp.data.models.Application;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.User;
import com.example.jobsearchapp.ui.base.BaseActivity;
import com.example.jobsearchapp.ui.employer.adapters.ApplicantAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewApplicantsActivity extends BaseActivity implements ApplicantAdapter.OnApplicantActionListener {

    private RecyclerView rvApplicants;
    private ImageView ivBack;
    private ApplicantAdapter adapter;
    private List<Applicant> applicantList = new ArrayList<>();
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutId() {
        return R.layout.employer_activity_view_applicants;
    }

    @Override
    protected void initViews() {
        rvApplicants = findViewById(R.id.rvApplicants);
        ivBack = findViewById(R.id.ivBack);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);

        rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicantAdapter(applicantList, this);
        rvApplicants.setAdapter(adapter);

        loadApplicants();
    }

    private void loadApplicants() {
        String employerId = sessionManager.getUserId();
        if (!employerId.isEmpty()) {
            // Sử dụng companyId làm fallback cho các đơn cũ chưa có employerId
            db.collection("applications")
                .whereEqualTo("companyId", employerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Applicant> applicants = new ArrayList<>();
                    if (queryDocumentSnapshots.isEmpty()) {
                        adapter.updateList(applicants);
                        return;
                    }

                    AtomicInteger counter = new AtomicInteger(0);
                    int total = queryDocumentSnapshots.size();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Application app = doc.toObject(Application.class);
                        if (app == null) {
                            if (counter.incrementAndGet() == total) {
                                applicantList = applicants;
                                adapter.updateList(applicantList);
                            }
                            continue;
                        }
                        app.setId(doc.getId());

                        // Fetch User and Job details for each application
                        if (app.getCandidateId() == null || app.getJobId() == null) {
                            if (counter.incrementAndGet() == total) {
                                applicantList = applicants;
                                adapter.updateList(applicantList);
                            }
                            continue;
                        }

                        db.collection("users").document(app.getCandidateId()).get()
                            .addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful() && userTask.getResult() != null && userTask.getResult().exists()) {
                                    User user = userTask.getResult().toObject(User.class);
                                    db.collection("jobs").document(app.getJobId()).get()
                                        .addOnCompleteListener(jobTask -> {
                                            if (jobTask.isSuccessful() && jobTask.getResult() != null && jobTask.getResult().exists()) {
                                                Job job = jobTask.getResult().toObject(Job.class);
                                                
                                                if (user != null) {
                                                    Applicant applicant = new Applicant();
                                                    applicant.setApplicationId(app.getId());
                                                    applicant.setId(userTask.getResult().getId());
                                                    applicant.setName(user.getFullName());
                                                    applicant.setEmail(user.getEmail());
                                                    applicant.setCvUrl(app.getCvUrl()); // Lấy URL CV từ đơn ứng tuyển
                                                    applicant.setJobTitle(job != null ? job.getTitle() : "N/A");
                                                    applicant.setJobId(app.getJobId());
                                                    applicant.setStatus(app.getStatus());
                                                    applicants.add(applicant);
                                                }
                                            }

                                            if (counter.incrementAndGet() == total) {
                                                applicantList = applicants;
                                                adapter.updateList(applicantList);
                                            }
                                        });
                                } else {
                                    if (counter.incrementAndGet() == total) {
                                        applicantList = applicants;
                                        adapter.updateList(applicantList);
                                    }
                                }
                            });
                    }
                })
                .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
        }
    }

    @Override
    protected void initListeners() {
        ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onAccept(Applicant applicant) {
        updateApplicationStatus(applicant.getApplicationId(), "Accepted");
    }

    @Override
    public void onReject(Applicant applicant) {
        updateApplicationStatus(applicant.getApplicationId(), "Rejected");
    }

    @Override
    public void onViewDetail(Applicant applicant) {
        android.content.Intent intent = new android.content.Intent(this, ApplicantDetailActivity.class);
        intent.putExtra("APPLICANT_DATA", applicant);
        startActivity(intent);
    }

    private void updateApplicationStatus(String applicationId, String status) {
        db.collection("applications").document(applicationId)
            .update("status", status)
            .addOnSuccessListener(aVoid -> {
                showToast("Đã " + (status.equals("Accepted") ? "chấp nhận" : "từ chối") + " đơn ứng tuyển");
                loadApplicants();
            })
            .addOnFailureListener(e -> showToast("Lỗi: " + e.getMessage()));
    }
}
