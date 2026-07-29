package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Category;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.activities.MainActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.CategoryAdapter;
import com.example.jobsearchapp.ui.candidate.adapters.JobAdapter;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends BaseFragment {

    private RecyclerView rvJobsMain, rvCategories, rvAiJobs;
    private ChipGroup cgTrending;
    private EditText edtSearch;
    private TextView tvViewAllCategories, tvViewMoreLatest, tvViewMoreAi;
    private View rlAiSuggestions;
    private android.widget.ProgressBar pbHome;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private JobAdapter jobAdapter, aiJobAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Job> aiJobList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_fragment_home;
    }

    @Override
    protected void initViews(View view) {
        rvJobsMain = view.findViewById(R.id.rvJobsMain);
        rvAiJobs = view.findViewById(R.id.rvAiJobs);
        rvCategories = view.findViewById(R.id.rvCategories);
        cgTrending = view.findViewById(R.id.cgTrending);
        edtSearch = view.findViewById(R.id.edtSearch);
        tvViewAllCategories = view.findViewById(R.id.tvViewAllCategories);
        tvViewMoreLatest = view.findViewById(R.id.tvViewMoreLatest);
        tvViewMoreAi = view.findViewById(R.id.tvViewMoreAi);
        rlAiSuggestions = view.findViewById(R.id.rlAiSuggestions);
        pbHome = view.findViewById(R.id.pbHome);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getContext());

        setupCategories();
        setupRecyclerView();
        setupTrendingKeywords();
        loadJobsFromFirebase();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh job list whenever user returns (e.g. after updating skills in Profile)
        loadJobsFromFirebase();
        loadSavedJobs();
    }

    private void loadSavedJobs() {
        String userId = sessionManager != null ? sessionManager.getUserId() : "";
        if (userId == null || userId.isEmpty()) return;

        db.collection("saved_jobs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> savedIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        savedIds.add(doc.getString("jobId"));
                    }
                    if (jobAdapter != null) {
                        jobAdapter.setSavedJobIds(savedIds);
                    }
                });
    }

    private void setupCategories() {
        categoryAdapter = new CategoryAdapter(categoryList, category -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSearch(category.getName());
            }
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupTrendingKeywords() {
        String[] keywords = {"React Native", "Digital Marketing", "Python", "Project Management", "UI/UX Design"};
        cgTrending.removeAllViews();
        for (String keyword : keywords) {
            Chip chip = new Chip(getContext());
            chip.setText(keyword);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeWidth(1f);
            chip.setChipStrokeColorResource(R.color.gray_border);
            chip.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    // Truyền keyword sang trang search
                    ((MainActivity) getActivity()).navigateToSearch(keyword);
                }
            });
            cgTrending.addView(chip);
        }
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(jobList);
        rvJobsMain.setLayoutManager(new LinearLayoutManager(getContext()));
        rvJobsMain.setAdapter(jobAdapter);

        aiJobAdapter = new JobAdapter(aiJobList, true);
        rvAiJobs.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        rvAiJobs.setAdapter(aiJobAdapter);
    }

    private void loadJobsFromFirebase() {
        if (pbHome != null) pbHome.setVisibility(View.VISIBLE);

        String role = sessionManager != null ? sessionManager.getRole() : "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // AI job recommendations are only available for candidates with an account
        boolean isCandidate = "candidate".equalsIgnoreCase(role) && user != null;

        if (isCandidate) {
            // Fetch AI-scored job_matches for this candidate first
            db.collection("job_matches").document(user.getUid()).collection("matches")
                    .get()
                    .addOnSuccessListener(matchDocs -> {
                        Map<String, Integer> aiScores = new HashMap<>();
                        for (QueryDocumentSnapshot doc : matchDocs) {
                            String jId = doc.getString("jobId");
                            Long score = doc.getLong("score");
                            if (jId != null && score != null) {
                                aiScores.put(jId, score.intValue());
                            }
                        }
                        fetchActualJobs(aiScores);
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.w("Home", "Could not load AI matches, falling back to latest jobs.", e);
                        fetchActualJobs(new HashMap<>());
                    });
        } else {
            // Guest or employer: show latest active jobs without AI scoring
            fetchActualJobs(new HashMap<>());
        }
    }

    private void fetchActualJobs(java.util.Map<String, Integer> aiScores) {
        db.collection("jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (pbHome != null) pbHome.setVisibility(View.GONE);
                    jobList.clear();
                    aiJobList.clear();
                    java.util.Set<String> uniqueCategories = new java.util.HashSet<>();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = mapDocToJob(doc);
                        
                        if (aiScores.containsKey(job.getId())) {
                            job.setAiScore(aiScores.get(job.getId()));
                        }

                        // Lọc phía Client: Chỉ hiện bài đăng "active"
                        if ("active".equalsIgnoreCase(job.getStatus())) {
                            if (job.getAiScore() != null && job.getAiScore() > 0) {
                                aiJobList.add(job);
                            } else {
                                jobList.add(job);
                            }
                            
                            if (job.getCategory() != null && !job.getCategory().isEmpty()) {
                                uniqueCategories.add(job.getCategory());
                            }
                        }
                    }
                    
                    // Sắp xếp AI Jobs theo điểm cao nhất
                    Collections.sort(aiJobList, (j1, j2) -> Integer.compare(j2.getAiScore(), j1.getAiScore()));
                    
                    // Sắp xếp Newest Jobs theo thời gian (giảm dần)
                    Collections.sort(jobList, (j1, j2) -> Long.compare(j2.getPostedAt(), j1.getPostedAt()));
                    
                    // Hiển thị section AI nếu có dữ liệu
                    if (!aiJobList.isEmpty()) {
                        if (rlAiSuggestions != null) rlAiSuggestions.setVisibility(View.VISIBLE);
                        if (rvAiJobs != null) rvAiJobs.setVisibility(View.VISIBLE);
                    } else {
                        if (rlAiSuggestions != null) rlAiSuggestions.setVisibility(View.GONE);
                        if (rvAiJobs != null) rvAiJobs.setVisibility(View.GONE);
                    }

                    // Giới hạn hiển thị Newest Jobs (ví dụ 10 bài)
                    if (jobList.size() > 10) {
                        List<Job> temp = new ArrayList<>(jobList.subList(0, 10));
                        jobList.clear();
                        jobList.addAll(temp);
                    }
                    
                    updateCategoryUI(uniqueCategories);
                    jobAdapter.notifyDataSetChanged();
                    aiJobAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (pbHome != null) pbHome.setVisibility(View.GONE);
                    android.util.Log.e("Home", "Error: " + e.getMessage());
                });
    }

    private Job mapDocToJob(com.google.firebase.firestore.DocumentSnapshot doc) {
        Job job = new Job();
        job.setId(doc.getId());
        job.setCompanyId(doc.getString("companyId"));
        job.setEmployerId(doc.getString("employerId"));
        job.setTitle(doc.getString("title"));
        job.setDescription(doc.getString("description"));
        job.setRequirements(doc.getString("requirements"));
        job.setBenefits(doc.getString("benefits"));
        
        Long sMin = doc.getLong("salaryMin");
        job.setSalaryMin(sMin != null ? sMin : 0);
        
        Long sMax = doc.getLong("salaryMax");
        job.setSalaryMax(sMax != null ? sMax : 0);
        
        job.setLocation(doc.getString("location"));
        job.setJobType(doc.getString("jobType"));
        job.setCategory(doc.getString("category"));
        job.setExperienceRequired(doc.getString("experienceRequired"));
        
        Long qty = doc.getLong("quantity");
        job.setQuantity(qty != null ? qty.intValue() : 1);

        job.setStatus(doc.getString("status"));
        
        job.setDeadlineFromObject(doc.get("deadline"));
        job.setPostedAtFromObject(doc.get("postedAt"));
        if (doc.contains("createdAt")) {
            job.setPostedAtFromObject(doc.get("createdAt"));
        }
        
        job.setCompanyName(doc.getString("companyName"));
        job.setLogoUrl(doc.getString("logoUrl"));
        
        // Bổ sung các trường có thể thiếu gây crash ở Detail
        if (job.getCompanyName() == null) job.setCompanyName("Công ty ẩn danh");
        if (job.getLocation() == null) job.setLocation("Chưa cập nhật địa điểm");
        if (job.getDescription() == null) job.setDescription("Không có mô tả");
        if (job.getRequirements() == null) job.setRequirements("Không có yêu cầu");

        return job;
    }

    private void updateCategoryUI(java.util.Set<String> categories) {
        categoryList.clear();
        int count = 0;
        for (String catName : categories) {
            if (count >= 4) break; // Tối đa 4 danh mục ở trang Home
            int iconRes = getIconForCategory(catName);
            categoryList.add(new Category(catName, iconRes));
            count++;
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private int getIconForCategory(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("it") || lower.contains("công nghệ") || lower.contains("phần mềm") || lower.contains("máy tính")) 
            return android.R.drawable.ic_menu_today;
        if (lower.contains("marketing") || lower.contains("tiếp thị")) 
            return android.R.drawable.ic_menu_send;
        if (lower.contains("kinh doanh") || lower.contains("sales") || lower.contains("bán hàng")) 
            return android.R.drawable.ic_menu_agenda;
        if (lower.contains("thiết kế") || lower.contains("design") || lower.contains("ux")) 
            return android.R.drawable.ic_menu_edit;
        if (lower.contains("nhân sự") || lower.contains("hr")) 
            return android.R.drawable.ic_menu_myplaces;
        if (lower.contains("kế toán") || lower.contains("tài chính") || lower.contains("phân tích")) 
            return android.R.drawable.ic_menu_view;
        if (lower.contains("an toàn") || lower.contains("bảo mật"))
            return android.R.drawable.ic_lock_lock;
        
        return android.R.drawable.ic_menu_directions; // Icon mặc định
    }

    @Override
    protected void initListeners() {
        if (edtSearch != null) {
            edtSearch.setFocusable(false);
            edtSearch.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToSearch(null);
                }
            });
        }

        if (tvViewAllCategories != null) {
            tvViewAllCategories.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToSearch(null);
                }
            });
        }

        if (tvViewMoreAi != null) {
            tvViewMoreAi.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToSearch(null);
                }
            });
        }

        if (tvViewMoreLatest != null) {
            tvViewMoreLatest.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToSearch(null);
                }
            });
        }
    }
}