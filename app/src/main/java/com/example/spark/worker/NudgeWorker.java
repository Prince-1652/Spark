package com.example.spark.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.spark.R;
import com.example.spark.data.AppDatabase;
import com.example.spark.data.Idea;
import com.example.spark.ui.welcome.WelcomeActivity;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NudgeWorker extends Worker {

    private static final String CHANNEL_ID = "spark_nudge_channel";
    private static final int NOTIFICATION_ID = 1001;

    public NudgeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        List<Idea> activeIdeas = db.ideaDao().getActiveIdeasSync();
        
        long fourteenDaysInMillis = TimeUnit.DAYS.toMillis(14);
        long currentTime = System.currentTimeMillis();

        Idea oldestInactiveIdea = null;
        if (activeIdeas != null) {
            for (Idea idea : activeIdeas) {
                if (currentTime - idea.getLastUpdated() >= fourteenDaysInMillis) {
                    oldestInactiveIdea = idea;
                    break;
                }
            }
        }

        if (oldestInactiveIdea != null) {
            sendNotification(oldestInactiveIdea);
        }

        return Result.success();
    }

    private void sendNotification(Idea idea) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Spark Nudges", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Spark Nudge")
                .setContentText("You haven't worked on '" + idea.getTitle() + "' in a while. Spark it up!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
