package com.example.spark.ui.home;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import com.example.spark.R;
import com.example.spark.data.Idea;

public class QuickCaptureDialog {

    public interface OnSaveListener {
        void onSave(Idea idea);
    }

    public static void show(Context context, OnSaveListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_quick_capture, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        EditText etQuickIdea = view.findViewById(R.id.etQuickIdea);
        view.findViewById(R.id.btnSaveSpark).setOnClickListener(v -> {
            String title = etQuickIdea.getText().toString().trim();
            if (!title.isEmpty()) {
                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    com.example.spark.data.AppDatabase db = com.example.spark.data.AppDatabase.getDatabase(context);
                    if (db.ideaDao().getIdeaByTitleSync(title) != null) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            com.example.spark.util.SparkToast.show((androidx.appcompat.app.AppCompatActivity) context, "Idea with same name exists");
                        });
                    } else {
                        Idea idea = new Idea(title, "", "Spark", System.currentTimeMillis(), false);
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            if (listener != null) listener.onSave(idea);
                            dialog.dismiss();
                        });
                    }
                });
            } else {
                etQuickIdea.setError("Title is empty");
            }
        });

        dialog.show();
        etQuickIdea.requestFocus();
    }
}
