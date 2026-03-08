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
import com.example.recipemasterpro.models.Reel;
import java.util.List;

public class ReelsAdapter extends RecyclerView.Adapter<ReelsAdapter.ReelViewHolder> {

    private Context context;
    private List<Reel> reelList;
    private OnReelClickListener listener;

    public interface OnReelClickListener {
        void onReelClick(Reel reel);
        void onLikeClick(Reel reel, int position);
        void onCommentClick(Reel reel);
        void onShareClick(Reel reel);
    }

    public ReelsAdapter(Context context, List<Reel> reelList, OnReelClickListener listener) {
        this.context = context;
        this.reelList = reelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        Reel reel = reelList.get(position);
        holder.bind(reel, position);
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    public class ReelViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage, profileImage;
        TextView titleText, chefNameText, likesText, commentsText;
        ImageView likeButton, commentButton, shareButton;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            profileImage = itemView.findViewById(R.id.profileImage);
            titleText = itemView.findViewById(R.id.titleText);
            chefNameText = itemView.findViewById(R.id.chefNameText);
            likesText = itemView.findViewById(R.id.likesText);
            commentsText = itemView.findViewById(R.id.commentsText);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            shareButton = itemView.findViewById(R.id.shareButton);
        }

        public void bind(Reel reel, int position) {
            titleText.setText(reel.getTitle());
            chefNameText.setText(reel.getChefName());
            likesText.setText(String.valueOf(reel.getLikes()));
            commentsText.setText(String.valueOf(reel.getComments()));

            // Load thumbnail
            if (reel.getThumbnailUrl() != null && !reel.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(reel.getThumbnailUrl())
                        .placeholder(R.drawable.ic_video_placeholder)
                        .into(thumbnailImage);
            }

            // Load profile image (placeholder for now)
            profileImage.setImageResource(R.drawable.ic_chef_placeholder);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReelClick(reel);
                }
            });

            likeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(reel, position);
                }
            });

            commentButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCommentClick(reel);
                }
            });

            shareButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShareClick(reel);
                }
            });
        }
    }
}