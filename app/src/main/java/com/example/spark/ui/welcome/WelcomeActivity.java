package com.example.spark.ui.welcome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.spark.R;
import com.example.spark.ui.home.HomeActivity;
import com.example.spark.util.NotificationHelper;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import com.example.spark.worker.NudgeWorker;
import java.util.concurrent.TimeUnit;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView sliderThumb;
    private View sliderFill;
    private FrameLayout sliderContainer;
    private TextView sliderText;
    private float startX;
    private boolean unlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        PeriodicWorkRequest nudgeWorkRequest = new PeriodicWorkRequest.Builder(NudgeWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("NudgeWork", ExistingPeriodicWorkPolicy.KEEP, nudgeWorkRequest);

        sliderThumb = findViewById(R.id.sliderThumb);
        sliderFill = findViewById(R.id.sliderFill);
        sliderContainer = findViewById(R.id.sliderContainer);
        sliderText = findViewById(R.id.sliderText);

        sliderThumb.setOnTouchListener((v, event) -> {
            int containerWidth = sliderContainer.getWidth();
            int thumbWidth = sliderThumb.getWidth();
            float maxTranslation = containerWidth - thumbWidth;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX() - sliderThumb.getTranslationX();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newX = event.getRawX() - startX;
                    newX = Math.max(0, Math.min(newX, maxTranslation));
                    sliderThumb.setTranslationX(newX);

                    // Update fill width to track with thumb center
                    ViewGroup.LayoutParams fillParams = sliderFill.getLayoutParams();
                    fillParams.width = (int) (newX + thumbWidth);
                    sliderFill.setLayoutParams(fillParams);

                    // Fade out hint text
                    float progress = newX / maxTranslation;
                    sliderText.setAlpha(1f - progress);

                    return true;

                case MotionEvent.ACTION_UP:
                    float currentX = sliderThumb.getTranslationX();
                    float progressPercent = currentX / maxTranslation;

                    if (progressPercent > 0.75f && !unlocked) {
                        unlocked = true;
                        

                        // Animate to end
                        sliderThumb.animate().translationX(maxTranslation).setDuration(150).start();
                        ViewGroup.LayoutParams fp = sliderFill.getLayoutParams();
                        fp.width = containerWidth;
                        sliderFill.setLayoutParams(fp);

                        sliderThumb.postDelayed(() -> {
                            Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }, 200);
                    } else {
                        // Snap back
                        sliderThumb.animate().translationX(0).setDuration(200).start();
                        ViewGroup.LayoutParams fp = sliderFill.getLayoutParams();
                        fp.width = 0;
                        sliderFill.setLayoutParams(fp);
                        sliderText.animate().alpha(1f).setDuration(200).start();
                    }
                    return true;
            }
            return false;
        });
    }
}
