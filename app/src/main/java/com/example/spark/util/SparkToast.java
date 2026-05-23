package com.example.spark.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spark.R;
import java.lang.ref.WeakReference;

public class SparkToast {

    private static WeakReference<View> currentToastRef = new WeakReference<>(null);
    private static Runnable currentDismissRunnable = null;

    public static void show(AppCompatActivity activity, String message) {
        ViewGroup rootView = activity.findViewById(android.R.id.content);

        // Cancel any existing toast immediately
        View existingToast = currentToastRef.get();
        if (existingToast != null) {
            existingToast.animate().cancel();
            if (currentDismissRunnable != null) {
                existingToast.removeCallbacks(currentDismissRunnable);
            }
            if (existingToast.getParent() != null) {
                ((ViewGroup) existingToast.getParent()).removeView(existingToast);
            }
            currentToastRef = new WeakReference<>(null);
            currentDismissRunnable = null;
        }

        View toastView = LayoutInflater.from(activity).inflate(R.layout.custom_toast, rootView, false);
        TextView textView = toastView.findViewById(R.id.toastText);
        textView.setText(message);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = 120;
        toastView.setLayoutParams(params);

        rootView.addView(toastView);
        currentToastRef = new WeakReference<>(toastView);

        toastView.setAlpha(0f);
        toastView.setTranslationY(40f);
        toastView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .start();

        Runnable dismissRunnable = () -> {
            View activeToast = currentToastRef.get();
            if (activeToast != null && activeToast == toastView) {
                toastView.animate()
                        .alpha(0f)
                        .translationY(40f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (toastView.getParent() != null) {
                                    ((ViewGroup) toastView.getParent()).removeView(toastView);
                                }
                                View stillActive = currentToastRef.get();
                                if (stillActive == toastView) {
                                    currentToastRef = new WeakReference<>(null);
                                    currentDismissRunnable = null;
                                }
                            }
                        })
                        .start();
            }
        };
        currentDismissRunnable = dismissRunnable;
        toastView.postDelayed(dismissRunnable, 1800);
    }
}

