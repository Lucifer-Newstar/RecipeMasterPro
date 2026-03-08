package com.example.recipemasterpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.recipemasterpro.R;

public class ReportsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // TODO: Implement reports functionality
        TextView placeholder = view.findViewById(R.id.placeholderText);
        placeholder.setText("Reports feature coming soon!");

        return view;
    }
}