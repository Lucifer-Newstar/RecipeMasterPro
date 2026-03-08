package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Rating;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RatingsAdapter extends RecyclerView.Adapter<RatingsAdapter.RatingViewHolder> {

    private Context context;
    private List<Rating> ratingList;
    private String currentUserId;
    private String recipeId;
    private OnRatingActionListener listener;
    private FirebaseFirestore db;

    public interface OnRatingActionListener {
        void onRatingDeleted();
        void onRatingEdited(Rating rating);
    }

    public RatingsAdapter(Context context, List<Rating> ratingList, String recipeId,
                          OnRatingActionListener listener) {
        this.context = context;
        this.ratingList = ratingList;
        this.recipeId = recipeId;
        this.listener = listener;
        this.currentUserId = SessionManager.getInstance(context).getUserId();
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        Rating rating = ratingList.get(position);

        holder.userNameText.setText(rating.getUserName());
        holder.ratingBar.setRating(rating.getRating());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String date = sdf.format(new Date(rating.getCreatedAt()));
        holder.dateText.setText(date);

        // Show review if exists
        if (rating.getReview() != null && !rating.getReview().isEmpty()) {
            holder.reviewText.setVisibility(View.VISIBLE);
            holder.reviewText.setText(rating.getReview());
        } else {
            holder.reviewText.setVisibility(View.GONE);
        }

        // Show action buttons if this is the user's own rating
        if (currentUserId != null && currentUserId.equals(rating.getUserId())) {
            holder.actionButtons.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRatingEdited(rating);
                }
            });

            holder.deleteButton.setOnClickListener(v -> {
                deleteRating(rating, position);
            });
        } else {
            holder.actionButtons.setVisibility(View.GONE);
        }
    }

    private void deleteRating(Rating rating, int position) {
        db.collection(Constants.RECIPES_COLLECTION)
                .document(recipeId)
                .collection(Constants.RATINGS_COLLECTION)
                .document(rating.getRatingId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    ratingList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, ratingList.size());

                    if (listener != null) {
                        listener.onRatingDeleted();
                    }

                    Toast.makeText(context, "Rating deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete rating", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }

    public static class RatingViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText, dateText, reviewText, editButton, deleteButton;
        RatingBar ratingBar;
        LinearLayout actionButtons;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            dateText = itemView.findViewById(R.id.dateText);
            reviewText = itemView.findViewById(R.id.reviewText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}