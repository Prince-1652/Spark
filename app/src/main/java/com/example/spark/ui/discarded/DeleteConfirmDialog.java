package com.example.spark.ui.discarded;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.example.spark.R;
import com.example.spark.data.Idea;

public class DeleteConfirmDialog {

    public interface OnDeleteListener {
        void onDelete(Idea idea);
    }
    
    public interface OnCancelListener {
        void onCancel();
    }

    public static void show(Context context, Idea idea, OnDeleteListener deleteListener, OnCancelListener cancelListener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_delete_confirmation, null);
        dialog.setContentView(view);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText("\"" + idea.getTitle() + "\" will be permanently deleted. This action cannot be undone.");

        view.findViewById(R.id.btnCancelDelete).setOnClickListener(v -> {
            if (cancelListener != null) cancelListener.onCancel();
            dialog.dismiss();
        });

        view.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(idea);
            dialog.dismiss();
        });

        dialog.show();
    }
}
