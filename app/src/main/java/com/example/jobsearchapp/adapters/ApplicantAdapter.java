package com.example.jobsearchapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.models.Applicant;
import java.util.List;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder> {

    private List<Applicant> applicantList;

    public ApplicantAdapter(List<Applicant> applicantList) {
        this.applicantList = applicantList;
    }

    @NonNull
    @Override
    public ApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_applicant, parent, false);
        return new ApplicantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantViewHolder holder, int position) {
        Applicant applicant = applicantList.get(position);
        holder.tvName.setText(applicant.getName());
        holder.tvEmail.setText(applicant.getEmail());
    }

    @Override
    public int getItemCount() {
        return applicantList != null ? applicantList.size() : 0;
    }

    static class ApplicantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;

        public ApplicantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvApplicantName);
            tvEmail = itemView.findViewById(R.id.tvApplicantEmail);
        }
    }
}