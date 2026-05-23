package com.example.spark.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ideas")
public class Idea {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    
    // Status can be: "Spark", "Brainstorming", "Discarded"
    private String status;
    
    // Unix timestamp
    private long lastUpdated;
    
    private boolean isPinned;

    public Idea(String title, String description, String status, long lastUpdated, boolean isPinned) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.isPinned = isPinned;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
}
