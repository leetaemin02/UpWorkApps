package com.example.jobsearchapp.ui.candidate.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Application;
import java.util.List;

public class AppliedJobAdapter extends RecyclerView.Adapter<AppliedJobAdapter.ViewHolder> {

    private List<Application> applicationList;

    public AppliedJobAdapter(List<Application> applicationList) {
        this.applicationList = applicationList;
    }

    public void setData(List<Application> list) {
        this.applicationList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.candidate_item_applied_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Application app = applicationList.get(position);
        holder.tvJobTitle.setText(app.getJobTitle() != null ? app.getJobTitle() : "Chức vụ không xác định");

        String status = app.getStatus();
        if ("pending".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Đang xem xét");
        } else {
            holder.tvStatus.setText(status != null ? status : "Đang chờ");
        }
    }

    @Override
    public int getItemCount() {
        return applicationList != null ? applicationList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvStatus = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}