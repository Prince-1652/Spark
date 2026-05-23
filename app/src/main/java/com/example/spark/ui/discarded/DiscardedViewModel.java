package com.example.spark.ui.discarded;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.spark.data.Idea;
import com.example.spark.data.IdeaRepository;
import java.util.List;

public class DiscardedViewModel extends AndroidViewModel {
    private IdeaRepository repository;
    private LiveData<List<Idea>> discardedIdeas;

    public DiscardedViewModel(@NonNull Application application) {
        super(application);
        repository = new IdeaRepository(application);
        discardedIdeas = repository.getDiscardedIdeas();
    }

    public LiveData<List<Idea>> getDiscardedIdeas() {
        return discardedIdeas;
    }

    public void update(Idea idea) {
        repository.updateInBackground(idea);
    }

    public void delete(Idea idea) {
        repository.delete(idea);
    }
}
