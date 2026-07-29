package com.example.jobsearchapp.ui.candidate.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.JobMatch;
import com.example.jobsearchapp.ui.activities.JobDetailActivity;
import java.util.List;

public class AiJobAdapter extends RecyclerView.Adapter<AiJobAdapter.ViewHolder> {

    private List<JobMatch> list;

    public AiJobAdapter(List<JobMatch> list) {
        this.list = list;
    }

    public void setData(List<JobMatch> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.candidate_item_job_ai, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JobMatch match = list.get(position);
        
        holder.tvAiScore.setText(match.getScore() + "% Match");
        holder.tvAiReason.setText(match.getReason());
        
        if (match.getJob() != null) {
            holder.tvTitle.setText(match.getJob().getTitle());
            holder.tvCompany.setText(match.getJob().getCompanyName());
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
                intent.putExtra("JOB_DATA", match.getJob());
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiScore, tvTitle, tvCompany, tvAiReason;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiScore = itemView.findViewById(R.id.tvAiScore);
            tvTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompany = itemView.findViewById(R.id.tvCompanyName);
            tvAiReason = itemView.findViewById(R.id.tvAiReason);
        }
    }
}
