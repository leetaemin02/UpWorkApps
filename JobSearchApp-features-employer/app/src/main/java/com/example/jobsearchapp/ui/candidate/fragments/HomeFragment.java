package com.example.jobsearchapp.ui.candidate.fragments;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.Category;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.activities.MainActivity;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.CategoryAdapter;
import com.example.jobsearchapp.ui.candidate.adapters.JobAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private RecyclerView rvJobsMain, rvCategories;
    private ChipGroup cgTrending;
    private EditText edtSearch;
    private TextView tvViewAllCategories;
    private FirebaseFirestore db;
    private JobAdapter jobAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Job> jobList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_fragment_home;
    }

    @Override
    protected void initViews(View view) {
        rvJobsMain = view.findViewById(R.id.rvJobsMain);
        rvCategories = view.findViewById(R.id.rvCategories);
        cgTrending = view.findViewById(R.id.cgTrending);
        edtSearch = view.findViewById(R.id.edtSearch);
        tvViewAllCategories = view.findViewById(R.id.tvViewAllCategories);

        db = FirebaseFirestore.getInstance();
        
        setupCategories();
        setupRecyclerView();
        setupTrendingKeywords();
        loadJobsFromFirebase();
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
    }

    private void loadJobsFromFirebase() {
        db.collection("jobs")
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .limit(50) // Tăng giới hạn để lấy được nhiều danh mục hơn
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    jobList.clear();
                    java.util.Set<String> uniqueCategories = new java.util.HashSet<>();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = mapDocToJob(doc);
                        jobList.add(job);
                        
                        if (job.getCategory() != null && !job.getCategory().isEmpty()) {
                            uniqueCategories.add(job.getCategory());
                        }
                    }
                    
                    updateCategoryUI(uniqueCategories);
                    jobAdapter.notifyDataSetChanged();
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
        for (String catName : categories) {
            int iconRes = getIconForCategory(catName);
            categoryList.add(new Category(catName, iconRes));
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private int getIconForCategory(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("it") || lower.contains("công nghệ") || lower.contains("phần mềm")) 
            return android.R.drawable.ic_menu_today;
        if (lower.contains("marketing") || lower.contains("tiếp thị")) 
            return android.R.drawable.ic_menu_send;
        if (lower.contains("kinh doanh") || lower.contains("sales")) 
            return android.R.drawable.ic_menu_agenda;
        if (lower.contains("thiết kế") || lower.contains("design")) 
            return android.R.drawable.ic_menu_edit;
        if (lower.contains("nhân sự") || lower.contains("hr")) 
            return android.R.drawable.ic_menu_myplaces;
        if (lower.contains("kế toán") || lower.contains("tài chính")) 
            return android.R.drawable.ic_menu_view;
        
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
    }
}