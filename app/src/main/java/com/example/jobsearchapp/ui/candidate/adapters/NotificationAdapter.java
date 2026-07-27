package com.example.jobsearchapp.ui.candidate.adapters;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobsearchapp.R;
import com.example.jobsearchapp.data.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> list;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> list, OnNotificationClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = list.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvBody.setText(notif.getMessage());
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(notif.getTimestamp(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.tvTime.setText(timeAgo);

        // Hiệu ứng chưa đọc
        if (!notif.isRead()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F1F8FF")); // Xanh rất nhạt
            holder.viewUnreadDot.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(Color.parseColor("#0D6EFD"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.viewUnreadDot.setVisibility(View.GONE);
            holder.tvTitle.setTextColor(Color.parseColor("#212529"));
        }

        // Icon mặc định
        holder.ivIcon.setImageResource(R.drawable.ic_pdf_red);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNotificationClick(notif);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void updateList(List<Notification> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBody, tvTime;
        ImageView ivIcon;
        View viewUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvBody = itemView.findViewById(R.id.tvNotifBody);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            ivIcon = itemView.findViewById(R.id.ivNotifIcon);
            viewUnreadDot = itemView.findViewById(R.id.viewUnreadDot);
        }
    }
}
