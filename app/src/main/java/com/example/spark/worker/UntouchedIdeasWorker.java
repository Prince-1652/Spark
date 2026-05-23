package com.example.spark.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.spark.data.AppDatabase;
import com.example.spark.data.Idea;
import com.example.spark.data.IdeaDao;
import com.example.spark.util.NotificationHelper;

import java.util.List;
import java.util.Random;

public class UntouchedIdeasWorker extends Worker {

    private static final String[] SINGLE_IDEA_TEXTS = {
            "It's been a while since you brainstormed on this idea. Give it some love?",
            "Your idea is getting dusty! Time to revisit it?",
            "Remember that brilliant spark? Don't let it fade away.",
            "You haven't touched this idea in a week. Let's expand on it!",
            "One of your ideas is waiting for you to ignite it.",
            "Time flies! Your idea from last week is asking for attention.",
            "Let's get back to brainstorming. Revisit your older idea now.",
            "Your creative spark needs fueling. Review this idea today.",
            "Don't let good ideas go to waste. Check back on what you wrote.",
            "A week has passed since you looked at this idea. Any new thoughts?"
    };

    private static final String[] MULTI_IDEA_TEXTS = {
            "You haven't brainstormed some ideas since a week. Let's revisit them!",
            "Several ideas are waiting for your attention. Time to ignite?",
            "Your brainstorming board has some untouched gems. Check them out.",
            "Don't let your past sparks fade. Revisit your older ideas.",
            "A few of your ideas haven't been touched in a week. Let's get creative!",
            "Time to review! Some of your ideas are getting older.",
            "Remember those brilliant thoughts? Revisit your week-old ideas.",
            "Your creative catalog has some untouched ideas. Give them a look.",
            "Let's get back to brainstorming those older concepts you saved.",
            "You have multiple ideas waiting for your spark. Check them out now."
    };

    public UntouchedIdeasWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        IdeaDao dao = AppDatabase.getDatabase(getApplicationContext()).ideaDao();
        
        // 7 days ago in milliseconds
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        List<Idea> untouchedIdeas = dao.getUntouchedIdeasSync(sevenDaysAgo);

        if (untouchedIdeas != null && !untouchedIdeas.isEmpty()) {
            Random random = new Random();
            if (untouchedIdeas.size() == 1) {
                Idea idea = untouchedIdeas.get(0);
                int index = random.nextInt(SINGLE_IDEA_TEXTS.length);
                String text = SINGLE_IDEA_TEXTS[index];
                NotificationHelper.showWeeklySingleIdeaNotification(
                        getApplicationContext(),
                        "Spark",
                        text,
                        idea.getId()
                );
            } else {
                int index = random.nextInt(MULTI_IDEA_TEXTS.length);
                String text = MULTI_IDEA_TEXTS[index];
                // showDailyNotification opens HomeActivity, which fits the multi-idea use case
                NotificationHelper.showDailyNotification(
                        getApplicationContext(),
                        "Spark",
                        text
                );
            }
        }

        return Result.success();
    }
}
