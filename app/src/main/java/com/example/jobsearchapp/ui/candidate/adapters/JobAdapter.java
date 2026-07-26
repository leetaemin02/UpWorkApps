package com.example.jobsearchapp.ui.candidate.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import com.example.jobsearchapp.ui.activities.JobDetailActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_FEATURED = 1;

    private List<Job> jobList;
    private List<Job> jobListFull;

    public JobAdapter(List<Job> jobList) {
        this.jobList = jobList;
        this.jobListFull = new ArrayList<>(jobList);
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
        // Bỏ logic đề xuất, luôn trả về kiểu bình thường để giao diện đồng nhất
        return TYPE_REGULAR;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Luôn sử dụng layout candidate_item_job cho tất cả công việc
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.candidate_item_job, parent, false);

        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {

        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());

        holder.tvSalary.setText(job.getSalaryMin() + " - " + job.getSalaryMax());

        holder.tvLocation.setText(job.getLocation());

        holder.tvCompanyName.setText(job.getCompanyName());

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

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvJobSalary);
            tvLocation = itemView.findViewById(R.id.tvJobLocation);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
        }
    }
}