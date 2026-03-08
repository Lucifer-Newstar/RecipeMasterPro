package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SuggestionViewHolder> {

    private Context context;
    private List<String> suggestions;
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String suggestion);
    }

    public SearchSuggestionAdapter(Context context, List<String> suggestions,
                                   OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String suggestion = suggestions.get(position);
        holder.suggestionText.setText(suggestion);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(suggestion);
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            // Remove from recent searches
            suggestions.remove(position);
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView suggestionText;
        ImageView removeButton;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            suggestionText = itemView.findViewById(R.id.suggestionText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}