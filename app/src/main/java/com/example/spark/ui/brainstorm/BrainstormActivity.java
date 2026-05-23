package com.example.spark.ui.brainstorm;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.spark.R;
import com.example.spark.data.Idea;
import com.example.spark.data.IdeaRepository;
import com.example.spark.util.SparkToast;
import java.util.concurrent.Executors;

public class BrainstormActivity extends AppCompatActivity {
    public static final String EXTRA_IDEA_ID = "extra_idea_id";
    
    private EditText etTitle;
    private EditText etDescription;
    private IdeaRepository repository;
    private Idea currentIdea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brainstorm);

        etTitle = findViewById(R.id.etBrainstormTitle);
        etDescription = findViewById(R.id.etBrainstormDescription);
        repository = new IdeaRepository(getApplication());

        int ideaId = getIntent().getIntExtra(EXTRA_IDEA_ID, -1);
        if (ideaId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                com.example.spark.data.AppDatabase db = com.example.spark.data.AppDatabase.getDatabase(getApplication());
                currentIdea = db.ideaDao().getIdeaById(ideaId);
                runOnUiThread(() -> {
                    if (currentIdea != null) {
                        etTitle.setText(currentIdea.getTitle());
                        etDescription.setText(currentIdea.getDescription());
                    }
                });
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    UnsavedChangesDialog.show(BrainstormActivity.this, () -> saveIdea(), () -> finish());
                } else {
                    finish();
                }
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btnSaveIdea).setOnClickListener(v -> saveIdea());
        
        findViewById(R.id.btnShareAI).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (title.isEmpty() && description.isEmpty()) return;
            
            String promptText = "Idea Title: " + title + "\nDescription: " + description;
            
            android.content.Intent sendIntent = new android.content.Intent();
            sendIntent.setAction(android.content.Intent.ACTION_SEND);
            sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, promptText);
            sendIntent.setType("text/plain");
            
            android.content.Intent shareIntent = android.content.Intent.createChooser(sendIntent, "Share idea with AI");
            startActivity(shareIntent);
        });

        findViewById(R.id.btnCopy).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            if (title.isEmpty() && description.isEmpty()) return;
            
            String copyText = "Idea Title: " + title + "\nDescription: " + description;
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Spark Idea", copyText);
            clipboard.setPrimaryClip(clip);
            
            SparkToast.show(this, "Copied to clipboard");
        });
    }

    private boolean hasUnsavedChanges() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (currentIdea == null) {
            return !title.isEmpty() || !description.isEmpty();
        } else {
            String originalTitle = currentIdea.getTitle() == null ? "" : currentIdea.getTitle().trim();
            String originalDesc = currentIdea.getDescription() == null ? "" : currentIdea.getDescription().trim();
            return !title.equals(originalTitle) || !description.equals(originalDesc);
        }
    }

    private void saveIdea() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            SparkToast.show(this, "Title cannot be empty");
            return;
        }

        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.example.spark.data.AppDatabase db = com.example.spark.data.AppDatabase.getDatabase(getApplicationContext());
            Idea existingIdea = db.ideaDao().getIdeaByTitleSync(title);
            
            if (existingIdea != null && (currentIdea == null || existingIdea.getId() != currentIdea.getId())) {
                runOnUiThread(() -> SparkToast.show(BrainstormActivity.this, "Idea with same name exists"));
                return;
            }
            
            if (currentIdea != null) {
                currentIdea.setTitle(title);
                currentIdea.setDescription(description);
                currentIdea.setStatus(description.isEmpty() ? "Spark" : "Brainstorming");
                currentIdea.setLastUpdated(System.currentTimeMillis());
                repository.updateInBackground(currentIdea);
            } else {
                String status = description.isEmpty() ? "Spark" : "Brainstorming";
                Idea idea = new Idea(title, description, status, System.currentTimeMillis(), false);
                repository.insert(idea);
            }
            
            runOnUiThread(() -> {
                SparkToast.show(BrainstormActivity.this, "Idea saved");
                finish();
            });
        });
    }
}
