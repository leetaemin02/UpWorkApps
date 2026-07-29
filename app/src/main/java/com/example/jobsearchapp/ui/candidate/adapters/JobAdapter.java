package com.example.jobsearchapp.ui.candidate.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.data.models.SavedJob;
import com.example.jobsearchapp.ui.activities.AuthActivity;
import com.example.jobsearchapp.ui.activities.JobDetailActivity;
import com.example.jobsearchapp.utils.FormatUtils;
import com.example.jobsearchapp.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_FEATURED = 1;

    private List<Job> jobList;
    private List<Job> jobListFull;
    private Set<String> savedJobIds = new HashSet<>();
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private boolean isHorizontal = false;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
        this.jobListFull = new ArrayList<>(jobList);
        this.db = FirebaseFirestore.getInstance();
    }

    public JobAdapter(List<Job> jobList, boolean isHorizontal) {
        this(jobList);
        this.isHorizontal = isHorizontal;
    }

    public void setSavedJobIds(Set<String> savedJobIds) {
        this.savedJobIds = savedJobIds;
        notifyDataSetChanged();
    }

    public void sort(boolean newestFirst) {
        Collections.sort(jobList, (j1, j2) -> {
            if (newestFirst) {
                return Long.compare(j2.getPostedAt(), j1.getPostedAt());
            } else {
                return Long.compare(j1.getPostedAt(), j2.getPostedAt());
            }
        });
        notifyDataSetChanged();
    }

    public void sortBySalary(boolean ascending) {
        Collections.sort(jobList, (j1, j2) -> {
            if (ascending) {
                return Long.compare(j1.getSalaryMin(), j2.getSalaryMin());
            } else {
                return Long.compare(j2.getSalaryMin(), j1.getSalaryMin());
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Luôn trả về kiểu bình thường để giao diện đồng nhất
        return TYPE_REGULAR;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isHorizontal ? R.layout.candidate_item_job_horizontal : R.layout.candidate_item_job;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {

        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());

        holder.tvSalary.setText(FormatUtils.formatSalaryRange(job.getSalaryMin(), job.getSalaryMax()));

        holder.tvLocation.setText(job.getLocation());

        // Mặc định nạp dữ liệu từ đối tượng job
        holder.tvCompanyName.setText(job.getCompanyName());
        holder.ivCompanyLogo.setImageResource(R.drawable.ic_default_avatar);

        // Tải thông tin công ty mới nhất từ bảng users
        if (job.getEmployerId() != null && !job.getEmployerId().isEmpty()) {
            db.collection("users").document(job.getEmployerId()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String latestName = documentSnapshot.getString("fullName");
                        String latestLogo = documentSnapshot.getString("avatarUrl");

                        if (latestName != null && !latestName.isEmpty()) {
                            holder.tvCompanyName.setText(latestName);
                        }
                        
                        if (latestLogo != null && !latestLogo.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                .load(latestLogo)
                                .circleCrop()
                                .placeholder(R.drawable.ic_default_avatar)
                                .into(holder.ivCompanyLogo);
                        }
                    }
                });
        }

        if (holder.tvCategory != null) {
            holder.tvCategory.setText(job.getCategory() != null ? job.getCategory() : "Khác");
        }

        if (holder.tvExp != null) {
            holder.tvExp.setText(job.getExperienceRequired() != null ? job.getExperienceRequired() : "Không yêu cầu");
        }

        if (holder.tvAiScore != null) {
            if (job.getAiScore() != null && job.getAiScore() > 0) {
                holder.tvAiScore.setVisibility(View.VISIBLE);
                holder.tvAiScore.setText(job.getAiScore() + "%");
            } else {
                holder.tvAiScore.setVisibility(View.GONE);
            }
        }

        // Bookmark Logic
        if (sessionManager == null) {
            sessionManager = new SessionManager(holder.itemView.getContext());
        }

        boolean isSaved = savedJobIds.contains(job.getId());
        holder.ivBookmark.setImageResource(isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.ivBookmark.setOnClickListener(v -> {
            String userId = sessionManager.getUserId();
            String role = sessionManager.getRole();

            if (userId.isEmpty()) {
                // Chưa đăng nhập -> Chuyển sang Auth
                v.getContext().startActivity(new Intent(v.getContext(), AuthActivity.class));
                return;
            }

            if (!"candidate".equalsIgnoreCase(role)) {
                // Đã đăng nhập nhưng không phải ứng viên
                Toast.makeText(v.getContext(), "Vui lòng đăng nhập tài khoản Ứng viên để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
                return;
            }

            // Xử lý lưu/bỏ lưu
            String docId = userId + "_" + job.getId();
            if (!isSaved) {
                SavedJob savedJob = new SavedJob(userId, job.getId());
                db.collection("saved_jobs").document(docId).set(savedJob)
                        .addOnSuccessListener(aVoid -> {
                            savedJobIds.add(job.getId());
                            notifyItemChanged(position);
                            Toast.makeText(v.getContext(), "Đã lưu công việc", Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("saved_jobs").document(docId).delete()
                        .addOnSuccessListener(aVoid -> {
                            savedJobIds.remove(job.getId());
                            notifyItemChanged(position);
                            Toast.makeText(v.getContext(), "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        android.widget.ImageView ivCompanyLogo = holder.itemView.findViewById(R.id.ivCompanyLogo);
        if (ivCompanyLogo != null) {
            // Load logo if needed
        }

        holder.itemView.setOnClickListener(v -> {
            if (job != null && job.getId() != null) {
                Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
                intent.putExtra("JOB_DATA", job);
                v.getContext().startActivity(intent);
            } else {
                android.widget.Toast.makeText(v.getContext(), "Không thể mở chi tiết: Dữ liệu công việc bị thiếu", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

    public void updateList(List<Job> newList) {
        jobList.clear();
        jobList.addAll(newList);
        jobListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query, List<String> selectedCategories, long minSalary, long maxSalary) {
        jobList.clear();
        String q = query.toLowerCase().trim();

        for (Job item : jobListFull) {
            // Keyword match
            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
            String comp = item.getCompanyName() == null ? "" : item.getCompanyName().toLowerCase();
            boolean matchQuery = q.isEmpty() || title.contains(q) || comp.contains(q);

            // Categories match (OR logic: if any selected category matches the job's category)
            boolean matchCategory = selectedCategories == null || selectedCategories.isEmpty() ||
                    (item.getCategory() != null && selectedCategories.contains(item.getCategory()));

            // Salary match
            boolean matchSalary = true;
            if (maxSalary > 0) {
                matchSalary = item.getSalaryMin() >= minSalary && item.getSalaryMin() <= maxSalary;
            } else if (minSalary > 0) {
                // For "Over X" case where maxSalary might be set to -1 or 0
                matchSalary = item.getSalaryMin() >= minSalary;
            }

            if (matchQuery && matchCategory && matchSalary) {
                jobList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    static class JobViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvSalary;
        TextView tvLocation;
        TextView tvCompanyName;
        TextView tvCategory;
        TextView tvExp;
        TextView tvAiScore;
        ImageView ivBookmark, ivCompanyLogo;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvJobSalary);
            tvLocation = itemView.findViewById(R.id.tvJobLocation);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvCategory = itemView.findViewById(R.id.tvJobCategory);
            tvExp = itemView.findViewById(R.id.tvJobExp);
            tvAiScore = itemView.findViewById(R.id.tvAiScore);
            ivBookmark = itemView.findViewById(R.id.ivBookmark);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
        }
    }
}