package com.example.spark;

import android.app.Application;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.spark.util.NotificationHelper;
import com.example.spark.worker.MorningNotificationWorker;
import com.example.spark.worker.UntouchedIdeasWorker;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SparkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this);

        // Schedule Workers
        scheduleMorningNotification();
        scheduleUntouchedIdeasCheck();
    }

    private void scheduleMorningNotification() {
        // We want it around 8:00 AM. For simplicity, we schedule it periodically every 24 hours.
        // We calculate initial delay until next 8:00 AM.
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        
        dueDate.set(Calendar.HOUR_OF_DAY, 8);
        dueDate.set(Calendar.MINUTE, 0);
        dueDate.set(Calendar.SECOND, 0);

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long initialDelay = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(MorningNotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MorningNotificationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
        );
    }

    private void scheduleUntouchedIdeasCheck() {
        // Run weekly
        PeriodicWorkRequest weeklyWorkRequest = new PeriodicWorkRequest.Builder(UntouchedIdeasWorker.class, 7, TimeUnit.DAYS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "UntouchedIdeasWork",
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyWorkRequest
        );
    }
}
