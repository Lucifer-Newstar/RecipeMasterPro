package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.activities.VideoPlayerActivity;
import com.example.recipemasterpro.models.Step;
import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private Context context;
    private List<Step> stepList;

    public StepAdapter(Context context, List<Step> stepList) {
        this.context = context;
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = stepList.get(position);

        holder.stepNumberText.setText("Step " + (position + 1));
        holder.instructionText.setText(step.getInstruction());

        if (step.getDuration() > 0) {
            holder.durationText.setText(step.getDuration() + " min");
        } else {
            holder.durationText.setVisibility(View.GONE);
        }

        // Check if video exists
        if (step.getVideoUrl() != null && !step.getVideoUrl().isEmpty()) {
            holder.watchVideoButton.setVisibility(View.VISIBLE);
            holder.watchVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open video player activity
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("video_url", step.getVideoUrl());
                    intent.putExtra("step_number", position + 1);
                    intent.putExtra("instruction", step.getInstruction());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.watchVideoButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumberText, instructionText, durationText;
        Button watchVideoButton;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            instructionText = itemView.findViewById(R.id.instructionText);
            durationText = itemView.findViewById(R.id.durationText);
            watchVideoButton = itemView.findViewById(R.id.watchVideoButton);
        }
    }
}