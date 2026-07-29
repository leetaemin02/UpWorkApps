package com.example.jobsearchapp.ui.candidate.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.ApplicationWithJob;
import com.example.jobsearchapp.ui.activities.JobDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppliedJobAdapter extends RecyclerView.Adapter<AppliedJobAdapter.ViewHolder> {

    private List<ApplicationWithJob> list;
    private FirebaseFirestore db;

    public AppliedJobAdapter(List<ApplicationWithJob> list) {
        this.list = list;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setData(List<ApplicationWithJob> newList) {
        this.list = newList;
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
        ApplicationWithJob item = list.get(position);
        if (item.job != null) {
            holder.tvJobTitle.setText(item.job.getTitle());

            // Mặc định nạp dữ liệu từ bài đăng
            holder.tvCompanyInfo.setText(item.job.getCompanyName() + " • " + item.job.getLocation());
            holder.ivCompanyLogo.setImageResource(R.drawable.ic_default_avatar);

            // Tải thông tin công ty mới nhất từ bảng users
            if (item.job.getEmployerId() != null && !item.job.getEmployerId().isEmpty()) {
                db.collection("users").document(item.job.getEmployerId()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String latestName = documentSnapshot.getString("fullName");
                                String latestLogo = documentSnapshot.getString("avatarUrl");

                                if (latestName != null && !latestName.isEmpty()) {
                                    holder.tvCompanyInfo.setText(latestName + " • " + item.job.getLocation());
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
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvAppliedDate.setText("Đã nộp: " + sdf.format(new Date(item.application.getAppliedAt())));

        String status = item.application.getStatus();
        if (status == null) status = "pending";

        // Đổi màu badge và dịch sang Tiếng Việt theo trạng thái
        switch (status.toLowerCase()) {
            case "accepted":
            case "reviewed":
                holder.tvStatusBadge.setText("Phỏng vấn");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#198754")); // Green
                break;
            case "rejected":
                holder.tvStatusBadge.setText("Từ chối");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#DC3545")); // Red
                break;
            case "pending":
            default:
                holder.tvStatusBadge.setText("Đang xem xét");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#0D6EFD")); // Blue
                break;
        }

        // Xử lý sự kiện click để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), JobDetailActivity.class);
            intent.putExtra("JOB_DATA", item.job);
            intent.putExtra("APPLICATION_DATA", item.application);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJobTitle, tvCompanyInfo, tvAppliedDate, tvStatusBadge;
        ImageView ivCompanyLogo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvCompanyInfo = itemView.findViewById(R.id.tvCompanyInfo);
            tvAppliedDate = itemView.findViewById(R.id.tvAppliedDate);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
        }
    }
}