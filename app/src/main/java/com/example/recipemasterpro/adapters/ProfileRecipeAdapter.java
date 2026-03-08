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
import com.example.recipemasterpro.models.Recipe;
import java.util.List;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public ProfileRecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        holder.titleText.setText(recipe.getTitle());
        holder.likesText.setText(String.valueOf(recipe.getLikes()));
        holder.viewsText.setText(String.valueOf(recipe.getViews()));

        if (recipe.getThumbnailUrl() != null && !recipe.getThumbnailUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getThumbnailUrl())
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .centerCrop()
                    .into(holder.thumbnailImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView titleText, likesText, viewsText;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            titleText = itemView.findViewById(R.id.titleText);
            likesText = itemView.findViewById(R.id.likesText);
            viewsText = itemView.findViewById(R.id.viewsText);
        }
    }
}