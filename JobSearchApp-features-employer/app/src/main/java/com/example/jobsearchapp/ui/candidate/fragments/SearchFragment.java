package com.example.jobsearchapp.ui.candidate.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.local.AppDatabase;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.base.BaseFragment;
import com.example.jobsearchapp.ui.candidate.adapters.JobAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment {
    
    private RecyclerView rvSearchResults;
    private JobAdapter adapter;
    private EditText edtSearch;
    private TextView tvResultCount, tvSort;
    private String currentTypeFilter = null;
    private String initialQuery = null;
    private FirebaseFirestore db;
    private List<Job> allJobs = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.candidate_fragment_search;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initialQuery = getArguments().getString("SEARCH_QUERY");
        }
    }

    @Override
    protected void initViews(View view) {
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        edtSearch = view.findViewById(R.id.edtSearch);
        tvResultCount = view.findViewById(R.id.tvResultCount);
        tvSort = view.findViewById(R.id.tvSort);
        
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupChips(view);
        loadJobsFromFirebase();
    }

    private void setupChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    currentTypeFilter = null;
                } else {
                    Chip chip = group.findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentTypeFilter = chip.getText().toString();
                    }
                }
                performSearch();
            });
        }
    }

    private void performSearch() {
        if (adapter != null && edtSearch != null) {
            String query = edtSearch.getText().toString();
            adapter.filter(query, currentTypeFilter);
            updateResultCount(adapter.getItemCount());
        }
    }

    @Override
    protected void initListeners() {
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    performSearch();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (tvSort != null) {
            tvSort.setOnClickListener(v -> showSortMenu());
        }
    }

    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(getContext(), tvSort);
        popup.getMenu().add("Mới nhất");
        popup.getMenu().add("Cũ nhất");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            tvSort.setText(title + " ▼");
            if (adapter != null) {
                adapter.sort(title.equals("Mới nhất"));
            }
            return true;
        });
        popup.show();
    }

    private void updateResultCount(int count) {
        if (tvResultCount != null) {
            tvResultCount.setText("Tìm thấy " + count + " kết quả");
        }
    }

    private void setupRecyclerView() {
        adapter = new JobAdapter(new ArrayList<>());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);
    }

    private void loadJobsFromFirebase() {
        db.collection("jobs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allJobs.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        allJobs.add(mapDocToJob(doc));
                    }
                    adapter.updateList(allJobs);
                    
                    if (initialQuery != null && edtSearch != null) {
                        edtSearch.setText(initialQuery);
                        performSearch();
                    }
                });
    }

    private Job mapDocToJob(com.google.firebase.firestore.DocumentSnapshot doc) {
        Job job = new Job();
        job.setId(doc.getId());
        job.setCompanyId(doc.getString("companyId"));
        job.setEmployerId(doc.getString("employerId"));
        job.setTitle(doc.getString("title"));
        job.setDescription(doc.getString("description"));
        
        Long sMin = doc.getLong("salaryMin");
        job.setSalaryMin(sMin != null ? sMin : 0);
        
        Long sMax = doc.getLong("salaryMax");
        job.setSalaryMax(sMax != null ? sMax : 0);
        
        job.setLocation(doc.getString("location"));
        job.setJobType(doc.getString("jobType"));
        job.setCategory(doc.getString("category"));
        job.setStatus(doc.getString("status"));
        
        job.setDeadlineFromObject(doc.get("deadline"));
        job.setPostedAtFromObject(doc.get("postedAt"));
        if (doc.contains("createdAt")) {
            job.setPostedAtFromObject(doc.get("createdAt"));
        }
        
        job.setCompanyName(doc.getString("companyName"));
        job.setLogoUrl(doc.getString("logoUrl"));

        // Bổ sung các trường có thể thiếu
        if (job.getCompanyName() == null) job.setCompanyName("Công ty ẩn danh");
        if (job.getLocation() == null) job.setLocation("Chưa cập nhật địa điểm");
        if (job.getDescription() == null) job.setDescription("Không có mô tả");

        return job;
    }
}