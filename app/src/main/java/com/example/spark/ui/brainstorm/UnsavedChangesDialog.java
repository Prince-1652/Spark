package com.example.spark.ui.brainstorm;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import com.example.spark.R;

public class UnsavedChangesDialog {

    public interface OnSaveAction {
        void onSave();
    }

    public interface OnCancelAction {
        void onCancel();
    }

    public static void show(Context context, OnSaveAction saveAction, OnCancelAction cancelAction) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_unsaved_changes, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.90);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        view.findViewById(R.id.btnCancelWarning).setOnClickListener(v -> {
            if (cancelAction != null) cancelAction.onCancel();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnSaveWarning).setOnClickListener(v -> {
            if (saveAction != null) {
                saveAction.onSave();
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
