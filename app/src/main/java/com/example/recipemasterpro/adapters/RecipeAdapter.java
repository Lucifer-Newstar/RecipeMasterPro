package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Recipe;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;

    // Interface for click listener
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    // Updated constructor with listener
    public RecipeAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, chefText, timeText;
        private OnRecipeClickListener listener;

        public RecipeViewHolder(@NonNull View itemView, OnRecipeClickListener listener) {
            super(itemView);
            this.listener = listener;
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            chefText = itemView.findViewById(R.id.chefText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        public void bind(final Recipe recipe) {
            titleText.setText(recipe.getTitle());
            descriptionText.setText(recipe.getDescription());
            chefText.setText("By: " + recipe.getChefName());

            if (recipe.getPrepTime() > 0 && recipe.getCookTime() > 0) {
                timeText.setText("Prep: " + recipe.getPrepTime() + "min | Cook: " + recipe.getCookTime() + "min");
            } else {
                timeText.setText("Time not specified");
            }

            // Set click listener on the entire item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onRecipeClick(recipe);
                    }
                }
            });
        }
    }
}
