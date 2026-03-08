package com.example.recipemasterpro.adapters.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;
    private String currentAdminId;

    public interface OnUserActionListener {
        void onUserClick(User user);
        void onUserSuspend(User user);
        void onUserDelete(User user);
        void onUserPromote(User user);
    }

    public UserAdminAdapter(Context context, List<User> userList, OnUserActionListener listener, String currentAdminId) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.currentAdminId = currentAdminId;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.nameText.setText(user.getName());
        holder.emailText.setText(user.getEmail());
        holder.roleText.setText(user.getRole());

        // Show stats
        String stats = "📊 " + user.getRecipeCount() + " recipes | " +
                user.getReelCount() + " reels | " +
                user.getFollowersCount() + " followers";
        holder.statsText.setText(stats);

        // Hide actions for current admin to prevent self-suspension/deletion
        if (user.getUserId().equals(currentAdminId)) {
            holder.suspendButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        } else {
            holder.suspendButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });

        holder.suspendButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserSuspend(user);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserDelete(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, emailText, roleText, statsText;
        Button suspendButton, deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.userNameText);
            emailText = itemView.findViewById(R.id.userEmailText);
            roleText = itemView.findViewById(R.id.userRoleText);
            statsText = itemView.findViewById(R.id.userStatsText);
            suspendButton = itemView.findViewById(R.id.suspendButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
