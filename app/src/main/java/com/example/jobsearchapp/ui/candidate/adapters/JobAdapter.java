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

    @Override
    public int getItemViewType(int position) {
        Job job = jobList.get(position);
        boolean isActive = job.getStatus() != null && job.getStatus().equals("active");
        return isActive && position % 5 == 0 ? TYPE_FEATURED : TYPE_REGULAR;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        int layout = (viewType == TYPE_FEATURED)
                ? R.layout.candidate_item_job_featured
                : R.layout.candidate_item_job;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {

        Job job = jobList.get(position);

        holder.tvTitle.setText(job.getTitle());

        holder.tvSalary.setText(job.getSalaryMin() + " - " + job.getSalaryMax());

        holder.tvLocation.setText(job.getLocation());

        holder.tvCompanyName.setText(job.getCompanyName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_DATA", job);
            v.getContext().startActivity(intent);
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

    public void filter(String query, String typeFilter) {

        jobList.clear();

        String q = query.toLowerCase().trim();

        for (Job item : jobListFull) {

            String title = item.getTitle() == null ? "" : item.getTitle().toLowerCase();
            String desc = item.getDescription() == null ? "" : item.getDescription().toLowerCase();

            boolean matchQuery =
                    q.isEmpty() ||
                            title.contains(q) ||
                            desc.contains(q);

            boolean matchType =
                    typeFilter == null ||
                            (item.getJobType() != null &&
                                    item.getJobType().equals(typeFilter));

            if (matchQuery && matchType) {
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