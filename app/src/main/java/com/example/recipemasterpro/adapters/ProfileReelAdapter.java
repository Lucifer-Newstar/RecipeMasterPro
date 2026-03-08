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

public class ProfileReelAdapter extends RecyclerView.Adapter<ProfileReelAdapter.ReelViewHolder> {

    private Context context;
    private List<Reel> reelList;
    private OnReelClickListener listener;

    public interface OnReelClickListener {
        void onReelClick(Reel reel);
    }

    public ProfileReelAdapter(Context context, List<Reel> reelList, OnReelClickListener listener) {
        this.context = context;
        this.reelList = reelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_reel, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        Reel reel = reelList.get(position);

        holder.titleText.setText(reel.getTitle());
        holder.likesText.setText(String.valueOf(reel.getLikes()));
        holder.viewsText.setText(String.valueOf(reel.getViews()));

        if (reel.getThumbnailUrl() != null && !reel.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(reel.getThumbnailUrl())
                    .placeholder(R.drawable.ic_reel_placeholder)
                    .centerCrop()
                    .into(holder.thumbnailImage);
        }

        holder.playButton.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReelClick(reel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reelList.size();
    }

    public static class ReelViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage, playButton;
        TextView titleText, likesText, viewsText;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            playButton = itemView.findViewById(R.id.playButton);
            titleText = itemView.findViewById(R.id.titleText);
            likesText = itemView.findViewById(R.id.likesText);
            viewsText = itemView.findViewById(R.id.viewsText);
        }
    }
}