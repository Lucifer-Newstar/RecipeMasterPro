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
import com.example.recipemasterpro.models.Comment;
import com.example.recipemasterpro.utils.SessionManager;
import de.hdodenhof.circleimageview.CircleImageView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private String currentUserId;
    private OnCommentActionListener listener;

    public interface OnCommentActionListener {
        void onDeleteClick(Comment comment, int position);
        void onCommentReport(Comment comment);
    }

    public CommentAdapter(Context context, List<Comment> commentList, OnCommentActionListener listener) {
        this.context = context;
        this.commentList = commentList;
        this.listener = listener;
        this.currentUserId = SessionManager.getInstance(context).getUserId();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        holder.userNameText.setText(comment.getUserName());
        holder.commentText.setText(comment.getComment());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        String time = sdf.format(new Date(comment.getTimestamp()));
        holder.timeText.setText(time);

        // Load user image using CircleImageView
        if (comment.getUserImageUrl() != null && !comment.getUserImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserImageUrl())
                    .placeholder(R.drawable.ic_chef_placeholder)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.ic_chef_placeholder);
        }

        // Show delete button only for user's own comments
        if (currentUserId != null && currentUserId.equals(comment.getUserId())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(comment, position);
                }
            });
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        // Report listener
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null && !comment.getUserId().equals(currentUserId)) {
                listener.onCommentReport(comment);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView userImage;
        ImageView deleteButton;
        TextView userNameText, commentText, timeText;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userNameText = itemView.findViewById(R.id.userNameText);
            commentText = itemView.findViewById(R.id.commentText);
            timeText = itemView.findViewById(R.id.timeText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
