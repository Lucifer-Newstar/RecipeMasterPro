package com.example.recipemasterpro.adapters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Recipe;
import com.example.recipemasterpro.models.Reel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContentAdminAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Object> contentList;
    private String contentType;
    private OnContentActionListener listener;

    public interface OnContentActionListener {
        void onContentClick(Object content);
        void onContentDelete(Object content);
    }

    public ContentAdminAdapter(Context context, List<Object> contentList, String contentType,
                               OnContentActionListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return contentType.equals("recipes") ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_recipe, parent, false);
            return new RecipeViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_reel, parent, false);
            return new ReelViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecipeViewHolder) {
            Recipe recipe = (Recipe) contentList.get(position);
            ((RecipeViewHolder) holder).bind(recipe);
        } else if (holder instanceof ReelViewHolder) {
            Reel reel = (Reel) contentList.get(position);
            ((ReelViewHolder) holder).bind(reel);
        }
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView titleText, chefText, statsText, dateText;
        Button deleteButton;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            titleText = itemView.findViewById(R.id.titleText);
            chefText = itemView.findViewById(R.id.chefText);
            statsText = itemView.findViewById(R.id.statsText);
            dateText = itemView.findViewById(R.id.dateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentClick(contentList.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentDelete(contentList.get(getAdapterPosition()));
                }
            });
        }

        void bind(Recipe recipe) {
            titleText.setText(recipe.getTitle());
            chefText.setText("By: " + recipe.getChefName());

            String stats = "👁️ " + recipe.getViews() + " | ❤️ " + recipe.getLikes() +
                    " | ⭐ " + String.format("%.1f", recipe.getAverageRating());
            statsText.setText(stats);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText(sdf.format(new Date(recipe.getCreatedAt())));

            if (recipe.getThumbnailUrl() != null && !recipe.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(recipe.getThumbnailUrl())
                        .placeholder(R.drawable.ic_recipe_placeholder)
                        .into(thumbnailImage);
            }
        }
    }

    class ReelViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView titleText, chefText, statsText, dateText;
        Button deleteButton;

        ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            titleText = itemView.findViewById(R.id.titleText);
            chefText = itemView.findViewById(R.id.chefText);
            statsText = itemView.findViewById(R.id.statsText);
            dateText = itemView.findViewById(R.id.dateText);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentClick(contentList.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContentDelete(contentList.get(getAdapterPosition()));
                }
            });
        }

        void bind(Reel reel) {
            titleText.setText(reel.getTitle());
            chefText.setText("By: " + reel.getChefName());

            String stats = "👁️ " + reel.getViews() + " | ❤️ " + reel.getLikes();
            statsText.setText(stats);

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateText.setText(sdf.format(new Date(reel.getCreatedAt())));

            if (reel.getThumbnailUrl() != null && !reel.getThumbnailUrl().isEmpty()) {
                Glide.with(context)
                        .load(reel.getThumbnailUrl())
                        .placeholder(R.drawable.ic_reel_placeholder)
                        .into(thumbnailImage);
            }
        }
    }
}