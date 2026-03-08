package com.example.recipemasterpro.adapters.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import java.util.List;

public class AdminActionsAdapter extends RecyclerView.Adapter<AdminActionsAdapter.ActionViewHolder> {

    private List<String> actions;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onActionClick(String action);
    }

    public AdminActionsAdapter(List<String> actions, OnActionClickListener listener) {
        this.actions = actions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_action, parent, false);
        return new ActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        String action = actions.get(position);
        holder.actionText.setText(action);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onActionClick(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    static class ActionViewHolder extends RecyclerView.ViewHolder {
        TextView actionText;

        ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            actionText = itemView.findViewById(R.id.actionText);
        }
    }
}
