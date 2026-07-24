package com.example.jobsearchapp.ui.employer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Job;
import java.util.List;

public class ManageJobAdapter extends RecyclerView.Adapter<ManageJobAdapter.ViewHolder> {

    private List<Job> jobList;
    private OnJobActionListener listener;

    public interface OnJobActionListener {
        void onEdit(Job job);
        void onDelete(Job job);
    }

    public ManageJobAdapter(List<Job> jobList, OnJobActionListener listener) {
        this.jobList = jobList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employer_item_manage_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.tvTitle.setText(job.getTitle());
        holder.tvSalary.setText("Lương: " + job.getSalaryMin() + " - " + job.getSalaryMax());
        holder.tvLocation.setText("Địa điểm: " + job.getLocation());

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(job));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(job));
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public void updateList(List<Job> newList) {
        this.jobList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSalary, tvLocation;
        View btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvSalary = itemView.findViewById(R.id.tvJobSalary);
            tvLocation = itemView.findViewById(R.id.tvJobLocation);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
