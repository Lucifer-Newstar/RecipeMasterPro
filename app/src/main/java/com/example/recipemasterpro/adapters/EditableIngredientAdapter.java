package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Ingredient;
import java.util.List;

public class EditableIngredientAdapter extends RecyclerView.Adapter<EditableIngredientAdapter.IngredientViewHolder> {

    private Context context;
    private List<Ingredient> ingredientList;

    public EditableIngredientAdapter(Context context, List<Ingredient> ingredientList) {
        this.context = context;
        this.ingredientList = ingredientList;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.nameText.setText(ingredient.getName());
        holder.quantityText.setText(ingredient.getQuantity() + " " + ingredient.getUnit());

        holder.removeButton.setOnClickListener(v -> {
            ingredientList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredientList.size());
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, quantityText;
        Button removeButton;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.ingredientNameText);
            quantityText = itemView.findViewById(R.id.ingredientQuantityText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}