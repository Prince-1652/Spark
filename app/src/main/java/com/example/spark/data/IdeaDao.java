package com.example.spark.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface IdeaDao {
    @Insert
    void insert(Idea idea);

    @Update
    void update(Idea idea);

    @Delete
    void delete(Idea idea);

    @Query("SELECT * FROM ideas WHERE status != 'Discarded' ORDER BY isPinned DESC, lastUpdated DESC")
    LiveData<List<Idea>> getActiveIdeas();

    @Query("SELECT * FROM ideas WHERE status != 'Discarded' ORDER BY isPinned DESC, lastUpdated DESC")
    List<Idea> getActiveIdeasSync();

    @Query("SELECT * FROM ideas WHERE status = 'Discarded' ORDER BY lastUpdated DESC")
    LiveData<List<Idea>> getDiscardedIdeas();

    @Query("SELECT * FROM ideas WHERE id = :id LIMIT 1")
    Idea getIdeaById(int id);

    @Query("SELECT * FROM ideas WHERE title = :title LIMIT 1")
    Idea getIdeaByTitleSync(String title);

    @Query("SELECT * FROM ideas WHERE status != 'Discarded' AND lastUpdated < :thresholdTimestamp")
    List<Idea> getUntouchedIdeasSync(long thresholdTimestamp);
}
