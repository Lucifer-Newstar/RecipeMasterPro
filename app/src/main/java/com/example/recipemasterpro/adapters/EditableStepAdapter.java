package com.example.recipemasterpro.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.models.Step;
import java.util.List;

public class EditableStepAdapter extends RecyclerView.Adapter<EditableStepAdapter.StepViewHolder> {

    private Context context;
    private List<Step> stepList;

    public EditableStepAdapter(Context context, List<Step> stepList) {
        this.context = context;
        this.stepList = stepList;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        Step step = stepList.get(position);
        holder.stepNumberText.setText("Step " + (position + 1));
        holder.instructionText.setText(step.getInstruction());

        if (step.getDuration() > 0) {
            holder.durationText.setText(step.getDuration() + " min");
        }

        holder.removeButton.setOnClickListener(v -> {
            stepList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, stepList.size());

            // Update step numbers
            for (int i = 0; i < stepList.size(); i++) {
                stepList.get(i).setStepNumber(i + 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stepList.size();
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepNumberText, instructionText, durationText;
        Button removeButton;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepNumberText = itemView.findViewById(R.id.stepNumberText);
            instructionText = itemView.findViewById(R.id.instructionText);
            durationText = itemView.findViewById(R.id.durationText);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}