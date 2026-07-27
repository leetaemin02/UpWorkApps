package com.example.jobsearchapp.ui.candidate.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private TextView tvResultCount, tvSort, tvSalaryFilter;
    private ChipGroup cgCategories;
    private LinearLayout layoutPagination;
    private android.widget.ProgressBar pbSearch;
    private List<String> selectedCategories = new ArrayList<>();
    private long minSalaryFilter = 0, maxSalaryFilter = 0;
    private String initialQuery = null;
    private FirebaseFirestore db;
    private List<Job> allJobs = new ArrayList<>();
    private List<Job> filteredJobs = new ArrayList<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 10;

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
        tvSalaryFilter = view.findViewById(R.id.tvSalaryFilter);
        cgCategories = view.findViewById(R.id.cgCategories);
        layoutPagination = view.findViewById(R.id.layoutPagination);
        pbSearch = view.findViewById(R.id.pbSearch);
        
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupSwipeNavigation();
        loadJobsFromFirebase();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedJobs();
    }

    private void loadSavedJobs() {
        com.example.jobsearchapp.utils.SessionManager sessionManager = new com.example.jobsearchapp.utils.SessionManager(getContext());
        String userId = sessionManager.getUserId();
        if (userId.isEmpty()) return;

        db.collection("saved_jobs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.Set<String> savedIds = new java.util.HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        savedIds.add(doc.getString("jobId"));
                    }
                    if (adapter != null) {
                        adapter.setSavedJobIds(savedIds);
                    }
                });
    }

    private void populateCategoryChips(java.util.Set<String> categories) {
        if (cgCategories == null || getContext() == null) return;
        cgCategories.removeAllViews();
        
        int[][] states = new int[][] {
            new int[] {android.R.attr.state_checked}, // checked
            new int[] {-android.R.attr.state_checked} // unchecked
        };

        for (String category : categories) {
            if (category == null || category.isEmpty()) continue;
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setClickable(true);

            // Hiệu ứng Tick và màu sắc khi chọn
            chip.setCheckedIconVisible(true);

            // Màu nền: Xanh nhạt khi chọn, Trắng khi không chọn
            int[] bgColors = new int[] {
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary_light),
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.white)
            };
            chip.setChipBackgroundColor(new android.content.res.ColorStateList(states, bgColors));

            // Màu viền: Xanh đậm khi chọn, Xám khi không chọn
            int[] strokeColors = new int[] {
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary),
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.gray_border)
            };
            chip.setChipStrokeColor(new android.content.res.ColorStateList(states, strokeColors));
            chip.setChipStrokeWidth(2f);

            // Màu chữ: Xanh đậm khi chọn, Đen khi không chọn
            int[] textColors = new int[] {
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary),
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_main)
            };
            chip.setTextColor(new android.content.res.ColorStateList(states, textColors));
            
            // Tự động tích chọn nếu danh mục này trùng với query từ Home
            if (initialQuery != null && initialQuery.equalsIgnoreCase(category)) {
                chip.setChecked(true);
                if (!selectedCategories.contains(category)) {
                    selectedCategories.add(category);
                }
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedCategories.contains(category)) selectedCategories.add(category);
                } else {
                    selectedCategories.remove(category);
                }
                currentPage = 1; // Reset về trang 1 khi lọc
                performSearch();
            });
            
            cgCategories.addView(chip);
        }
    }

    private void performSearch() {
        if (allJobs.isEmpty()) return;
        
        String query = (edtSearch != null) ? edtSearch.getText().toString().toLowerCase().trim() : "";
        filteredJobs.clear();

        for (Job item : allJobs) {
            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
            String comp = item.getCompanyName() == null ? "" : item.getCompanyName().toLowerCase();
            boolean matchQuery = query.isEmpty() || title.contains(query) || comp.contains(query);

            boolean matchCategory = selectedCategories.isEmpty() ||
                    (item.getCategory() != null && selectedCategories.contains(item.getCategory()));

            boolean matchSalary = true;
            if (maxSalaryFilter > 0) {
                matchSalary = item.getSalaryMin() >= minSalaryFilter && item.getSalaryMin() <= maxSalaryFilter;
            } else if (minSalaryFilter > 0) {
                matchSalary = item.getSalaryMin() >= minSalaryFilter;
            }

            if (matchQuery && matchCategory && matchSalary) {
                filteredJobs.add(item);
            }
        }

        updateResultCount(filteredJobs.size());
        updatePaginationUI();
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        if (adapter == null) return;
        
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredJobs.size());
        
        if (start < filteredJobs.size()) {
            List<Job> pageItems = filteredJobs.subList(start, end);
            adapter.updateList(new ArrayList<>(pageItems));
        } else {
            adapter.updateList(new ArrayList<>());
        }
    }

    private void updatePaginationUI() {
        if (layoutPagination == null || getContext() == null) return;
        layoutPagination.removeAllViews();
        
        int totalPages = (int) Math.ceil((double) filteredJobs.size() / PAGE_SIZE);
        if (totalPages <= 1) return;

        int startPage, endPage;
        boolean showStartDots = false;
        boolean showEndDots = false;

        if (totalPages <= 5) {
            startPage = 1;
            endPage = totalPages;
        } else {
            if (currentPage <= 3) {
                startPage = 1;
                endPage = 4;
                showEndDots = true;
            } else if (currentPage >= totalPages - 2) {
                startPage = totalPages - 3;
                endPage = totalPages;
                showStartDots = true;
            } else {
                startPage = currentPage - 1;
                endPage = currentPage + 1;
                showStartDots = true;
                showEndDots = true;
            }
        }

        // Trang đầu
        if (showStartDots) {
            addPageButton("1", currentPage == 1, () -> {
                currentPage = 1;
                refreshPage();
            });
            addDots();
        }

        // Các trang ở giữa
        for (int i = startPage; i <= endPage; i++) {
            final int pageNum = i;
            addPageButton(String.valueOf(pageNum), pageNum == currentPage, () -> {
                currentPage = pageNum;
                refreshPage();
            });
        }

        // Dấu ba chấm và trang cuối
        if (showEndDots) {
            addDots();
            addPageButton(String.valueOf(totalPages), currentPage == totalPages, () -> {
                currentPage = totalPages;
                refreshPage();
            });
        }
    }

    private void addDots() {
        if (getContext() == null) return;
        TextView tv = new TextView(getContext());
        tv.setText("...");
        tv.setPadding(12, 0, 12, 0);
        tv.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_sub));
        layoutPagination.addView(tv);
    }

    private void addPageButton(String text, boolean isSelected, Runnable onClick) {
        if (getContext() == null) return;
        TextView btn = new TextView(getContext());
        int size = (int) (38 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 0, 8, 0);
        btn.setLayoutParams(params);
        btn.setText(text);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setTextSize(14);
        btn.setBackgroundResource(R.drawable.bg_page_item);
        btn.setSelected(isSelected);
        
        if (isSelected) {
            btn.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.white));
        } else {
            btn.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.primary));
        }

        btn.setOnClickListener(v -> onClick.run());
        layoutPagination.addView(btn);
    }

    private void refreshPage() {
        updatePaginationUI();
        displayCurrentPage();
        if (rvSearchResults != null) rvSearchResults.scrollToPosition(0);
    }

    private void setupSwipeNavigation() {
        final android.view.GestureDetector gestureDetector = new android.view.GestureDetector(getContext(), 
            new android.view.GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(android.view.MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2, float velocityX, float velocityY) {
                    if (e1 == null || e2 == null) return false;
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                        int totalPages = (int) Math.ceil((double) filteredJobs.size() / PAGE_SIZE);
                        if (diffX < 0) {
                            if (currentPage < totalPages) {
                                currentPage++;
                                refreshPage();
                                return true;
                            }
                        } else {
                            if (currentPage > 1) {
                                currentPage--;
                                refreshPage();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });

        rvSearchResults.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    @Override
    protected void initListeners() {
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentPage = 1;
                    performSearch();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (tvSort != null) {
            tvSort.setOnClickListener(v -> showSortMenu());
        }

        if (tvSalaryFilter != null) {
            tvSalaryFilter.setOnClickListener(v -> showSalaryFilterMenu());
        }
    }

    private void showSalaryFilterMenu() {
        PopupMenu popup = new PopupMenu(getContext(), tvSalaryFilter);
        popup.getMenu().add("Tất cả mức lương");
        popup.getMenu().add("Dưới 10 triệu");
        popup.getMenu().add("10 - 20 triệu");
        popup.getMenu().add("Trên 20 triệu");
        popup.getMenu().add("-----------------");
        popup.getMenu().add("Lương: Thấp đến Cao");
        popup.getMenu().add("Lương: Cao đến Thấp");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("-----------------")) return false;

            if (title.contains("Lương:")) {
                if (adapter != null) {
                    adapter.sortBySalary(title.contains("Thấp đến Cao"));
                    displayCurrentPage();
                }
            } else {
                tvSalaryFilter.setText(title + " ▼");
                if (title.equals("Tất cả mức lương")) {
                    minSalaryFilter = 0; maxSalaryFilter = 0;
                } else if (title.equals("Dưới 10 triệu")) {
                    minSalaryFilter = 0; maxSalaryFilter = 10000000;
                } else if (title.equals("10 - 20 triệu")) {
                    minSalaryFilter = 10000000; maxSalaryFilter = 20000000;
                } else if (title.equals("Trên 20 triệu")) {
                    minSalaryFilter = 20000000; maxSalaryFilter = 0;
                }
                currentPage = 1;
                performSearch();
            }
            return true;
        });
        popup.show();
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
                displayCurrentPage();
            }
            return true;
        });
        popup.show();
    }

    private void updateResultCount(int count) {
        if (tvResultCount != null) {
            tvResultCount.setText("Tìm thấy " + count + " việc làm phù hợp");
        }
    }

    private void setupRecyclerView() {
        adapter = new JobAdapter(new ArrayList<>());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);
    }

    private void loadJobsFromFirebase() {
        if (pbSearch != null) pbSearch.setVisibility(View.VISIBLE);
        db.collection("jobs")
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (pbSearch != null) pbSearch.setVisibility(View.GONE);
                    allJobs.clear();
                    filteredJobs.clear();
                    java.util.Set<String> uniqueCategories = new java.util.TreeSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Job job = mapDocToJob(doc);
                        // Lọc phía Client: Chỉ hiện bài đăng "active"
                        if ("active".equalsIgnoreCase(job.getStatus())) {
                            allJobs.add(job);
                        }
                    }
                    
                    // Sắp xếp thủ công
                    java.util.Collections.sort(allJobs, (j1, j2) -> Long.compare(j2.getPostedAt(), j1.getPostedAt()));
                    
                    boolean matchedCategory = false;
                    for (Job job : allJobs) {
                        if (job.getCategory() != null && !job.getCategory().isEmpty()) {
                            uniqueCategories.add(job.getCategory());
                            if (initialQuery != null && initialQuery.equalsIgnoreCase(job.getCategory())) {
                                matchedCategory = true;
                            }
                        }
                    }
                    
                    populateCategoryChips(uniqueCategories);
                    
                    // Nếu là danh mục thì không hiện lên ô search, nếu là từ khóa thường thì hiện để lọc
                    if (initialQuery != null && !matchedCategory && edtSearch != null) {
                        edtSearch.setText(initialQuery);
                    }
                    
                    performSearch();
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

        // Bổ sung các trường có thể thiếu
        if (job.getCompanyName() == null) job.setCompanyName("Công ty ẩn danh");
        if (job.getLocation() == null) job.setLocation("Chưa cập nhật địa điểm");
        if (job.getDescription() == null) job.setDescription("Không có mô tả");

        return job;
    }
}