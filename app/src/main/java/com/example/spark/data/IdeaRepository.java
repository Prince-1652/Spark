package com.example.spark.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdeaRepository {
    private IdeaDao ideaDao;
    private LiveData<List<Idea>> activeIdeas;
    private LiveData<List<Idea>> discardedIdeas;

    // Use a single thread executor for database operations
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public IdeaRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        ideaDao = db.ideaDao();
        activeIdeas = ideaDao.getActiveIdeas();
        discardedIdeas = ideaDao.getDiscardedIdeas();
    }

    public LiveData<List<Idea>> getActiveIdeas() {
        return activeIdeas;
    }

    public LiveData<List<Idea>> getDiscardedIdeas() {
        return discardedIdeas;
    }

    public void insert(Idea idea) {
        executorService.execute(() -> ideaDao.insert(idea));
    }

    public void update(Idea idea) {
        ideaDao.update(idea);
    }
    
    public void updateInBackground(Idea idea) {
        executorService.execute(() -> ideaDao.update(idea));
    }

    public void delete(Idea idea) {
        executorService.execute(() -> ideaDao.delete(idea));
    }
}
