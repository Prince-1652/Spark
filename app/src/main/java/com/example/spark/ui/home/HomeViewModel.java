package com.example.spark.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.spark.data.Idea;
import com.example.spark.data.IdeaRepository;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private IdeaRepository repository;
    private LiveData<List<Idea>> activeIdeas;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new IdeaRepository(application);
        activeIdeas = repository.getActiveIdeas();
    }

    public LiveData<List<Idea>> getActiveIdeas() {
        return activeIdeas;
    }

    public void update(Idea idea) {
        repository.updateInBackground(idea);
    }

    public void insert(Idea idea) {
        repository.insert(idea);
    }
}
