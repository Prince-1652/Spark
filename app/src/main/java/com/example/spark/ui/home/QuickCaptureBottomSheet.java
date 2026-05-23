package com.example.spark.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.spark.R;
import com.example.spark.data.Idea;
import com.example.spark.data.IdeaRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class QuickCaptureBottomSheet extends BottomSheetDialogFragment {

    private EditText etQuickIdea;
    private HomeViewModel homeViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_quick_capture, container, false);
        etQuickIdea = view.findViewById(R.id.etQuickIdea);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        view.findViewById(R.id.btnSaveSpark).setOnClickListener(v -> {
            String title = etQuickIdea.getText().toString().trim();
            if (!title.isEmpty()) {
                Idea idea = new Idea(title, "", "Spark", System.currentTimeMillis(), false);
                homeViewModel.insert(idea);
                dismiss();
            } else {
                etQuickIdea.setError("Title is empty");
            }
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        etQuickIdea.requestFocus();
        etQuickIdea.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etQuickIdea, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }
}
