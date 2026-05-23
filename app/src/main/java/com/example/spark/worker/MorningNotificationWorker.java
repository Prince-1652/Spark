package com.example.spark.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.spark.util.NotificationHelper;
import java.util.Random;

public class MorningNotificationWorker extends Worker {

    private static final String[] MORNING_TEXTS = {
            "Good morning! Got any spark in your mind?",
            "Rise and shine! Ready to capture some brilliant ideas today?",
            "A new day brings new inspiration. What are you brainstorming today?",
            "Good morning! Don't let those fleeting thoughts escape, capture them now!",
            "Hello there! The best ideas start with a single spark. Let's get creative!",
            "Morning! Your next big idea is waiting to be written down.",
            "Wakey wakey! Time to ignite your creativity.",
            "Good morning! Every great invention started with a simple thought.",
            "A fresh morning is the perfect canvas for your mind's sparks.",
            "Good morning! Keep your spark alive and brainstorm your heart out!"
    };

    public MorningNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Random random = new Random();
        int index = random.nextInt(MORNING_TEXTS.length);
        String text = MORNING_TEXTS[index];

        NotificationHelper.showDailyNotification(
                getApplicationContext(),
                "Spark",
                text
        );

        return Result.success();
    }
}
