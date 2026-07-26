package com.example.jobsearchapp.ui.employer.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Applicant;
import java.util.List;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder> {

    private List<Applicant> applicantList;
    private OnApplicantActionListener listener;

    public interface OnApplicantActionListener {
        void onAccept(Applicant applicant);
        void onReject(Applicant applicant);
        void onViewDetail(Applicant applicant);
    }

    public ApplicantAdapter(List<Applicant> applicantList, OnApplicantActionListener listener) {
        this.applicantList = applicantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employer_item_applicant, parent, false);
        return new ApplicantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantViewHolder holder, int position) {
        Applicant applicant = applicantList.get(position);
        holder.tvName.setText(applicant.getName());
        holder.tvAppliedJob.setText("Vị trí: " + applicant.getJobTitle());

        String status = applicant.getStatus();
        holder.tvStatus.setText("Trạng thái: " + translateStatus(status));

        // Đổi màu text trạng thái
        if ("Accepted".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#198754")); 
            holder.layoutActions.setVisibility(View.GONE);
        } else if ("Rejected".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#DC3545")); 
            holder.layoutActions.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#6C757D")); 
            holder.layoutActions.setVisibility(View.VISIBLE);
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(applicant));
        holder.btnReject.setOnClickListener(v -> listener.onReject(applicant));
        
        // Nhấn vào toàn bộ item để xem chi tiết
        holder.itemView.setOnClickListener(v -> listener.onViewDetail(applicant));
    }

    private String translateStatus(String status) {
        if (status == null) return "Chưa có";
        if ("pending".equalsIgnoreCase(status)) return "Đang chờ";
        if ("Accepted".equalsIgnoreCase(status)) return "Đã chấp nhận";
        if ("Rejected".equalsIgnoreCase(status)) return "Đã từ chối";
        return status;
    }

    @Override
    public int getItemCount() {
        return applicantList != null ? applicantList.size() : 0;
    }

    public void updateList(List<Applicant> newList) {
        this.applicantList = newList;
        notifyDataSetChanged();
    }

    static class ApplicantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAppliedJob, tvStatus;
        View btnAccept, btnReject, layoutActions;

        public ApplicantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvAppliedJob = itemView.findViewById(R.id.tvAppliedJob);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            layoutActions = itemView.findViewById(R.id.layoutActions);
        }
    }
}
