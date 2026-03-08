package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList,
                               OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.messageText.setText(notification.getMessage());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(notification.getTimestamp()));
        holder.timeText.setText(time);

        // Set icon based on type
        switch (notification.getType()) {
            case "follow":
                holder.iconImage.setImageResource(R.drawable.ic_follow);
                break;
            case "comment":
                holder.iconImage.setImageResource(R.drawable.ic_comment);
                break;
            case "like":
                holder.iconImage.setImageResource(R.drawable.ic_like);
                break;
            case "new_recipe":
                holder.iconImage.setImageResource(R.drawable.ic_new_recipe);
                break;
        }

        // Highlight if unread
        if (!notification.isRead()) {
            holder.itemView.setBackgroundColor(context.getColor(R.color.unread_background));
        } else {
            holder.itemView.setBackgroundColor(context.getColor(android.R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImage;
        TextView messageText, timeText;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.iconImage);
            messageText = itemView.findViewById(R.id.messageText);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
}
