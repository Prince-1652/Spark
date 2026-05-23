package com.example.spark.ui.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spark.R;
import com.example.spark.data.Idea;
import com.example.spark.util.SparkToast;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private java.util.List<Idea> allIdeas = new java.util.ArrayList<>();
    private String currentFilter = "All"; // "All", "Spark", "Brainstorming"
    private IdeaAdapter adapter;
    private TextView emptyState;
    private RecyclerView recyclerView;
    private TextView tvFilterLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.ideasRecyclerView);
        emptyState = findViewById(R.id.emptyStateText);
        TextView tvSparksCount = findViewById(R.id.tvSparksCount);
        TextView tvBrainstormedCount = findViewById(R.id.tvBrainstormedCount);
        tvFilterLabel = findViewById(R.id.tvFilterLabel);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Disable change animation to prevent crossfade glitches during expand/collapse
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof androidx.recyclerview.widget.SimpleItemAnimator) {
            ((androidx.recyclerview.widget.SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        adapter = new IdeaAdapter();
        recyclerView.setAdapter(adapter);

        View sparksTile = (View) tvSparksCount.getParent();
        View brainstormedTile = (View) tvBrainstormedCount.getParent();

        sparksTile.setOnClickListener(v -> {
            if (currentFilter.equals("Spark")) {
                currentFilter = "All";
                tvFilterLabel.setVisibility(View.GONE);
            } else {
                currentFilter = "Spark";
                tvFilterLabel.setText("Showing Sparks  ✕");
                tvFilterLabel.setVisibility(View.VISIBLE);
            }
            applyFilter();
        });

        brainstormedTile.setOnClickListener(v -> {
            if (currentFilter.equals("Brainstorming")) {
                currentFilter = "All";
                tvFilterLabel.setVisibility(View.GONE);
            } else {
                currentFilter = "Brainstorming";
                tvFilterLabel.setText("Showing Brainstormed  ✕");
                tvFilterLabel.setVisibility(View.VISIBLE);
            }
            applyFilter();
        });

        tvFilterLabel.setOnClickListener(v -> {
            currentFilter = "All";
            tvFilterLabel.setVisibility(View.GONE);
            applyFilter();
        });

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getActiveIdeas().observe(this, ideas -> {
            allIdeas = ideas == null ? new java.util.ArrayList<>() : ideas;
            
            // Calculate Stats
            int sparks = 0;
            int brainstormed = 0;
            for (Idea idea : allIdeas) {
                if ("Spark".equals(idea.getStatus())) {
                    sparks++;
                } else if ("Brainstorming".equals(idea.getStatus())) {
                    brainstormed++;
                }
            }
            
            tvSparksCount.setText(String.valueOf(sparks));
            tvBrainstormedCount.setText(String.valueOf(brainstormed));
            
            applyFilter();
        });

        adapter.setOnItemClickListener(idea -> {
            android.content.Intent intent = new android.content.Intent(HomeActivity.this, com.example.spark.ui.brainstorm.BrainstormActivity.class);
            intent.putExtra(com.example.spark.ui.brainstorm.BrainstormActivity.EXTRA_IDEA_ID, idea.getId());
            startActivity(intent);
        });

        View tutorialOverlay = findViewById(R.id.tutorialOverlay);
        android.content.SharedPreferences prefs = getSharedPreferences("SparkPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("hasSeenTutorial", false)) {
            tutorialOverlay.setVisibility(View.VISIBLE);
            tutorialOverlay.setOnClickListener(v -> {
                tutorialOverlay.setVisibility(View.GONE);
                prefs.edit().putBoolean("hasSeenTutorial", true).apply();
            });
        }

        // Swipe to discard with icon background
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Paint paint = new Paint();
            private final android.graphics.drawable.Drawable deleteIcon = ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_delete_grey);
            private final android.graphics.drawable.Drawable pinIcon = ContextCompat.getDrawable(HomeActivity.this, R.drawable.ic_pin);

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Idea idea = adapter.getIdeaAt(position);
                    if (idea.isPinned()) {
                        // Only allow right swipe (to unpin) for pinned ideas
                        // Left swipe drag is handled visually in onChildDraw
                        return ItemTouchHelper.RIGHT;
                    }
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Idea swipedIdea = adapter.getIdeaAt(position);
                
                if (direction == ItemTouchHelper.LEFT) {
                    swipedIdea.setStatus("Discarded");
                    swipedIdea.setLastUpdated(System.currentTimeMillis());
                    homeViewModel.update(swipedIdea);
                    SparkToast.show(HomeActivity.this, "Idea moved to graveyard");
                } else if (direction == ItemTouchHelper.RIGHT) {
                    swipedIdea.setPinned(!swipedIdea.isPinned());
                    swipedIdea.setLastUpdated(System.currentTimeMillis());
                    homeViewModel.update(swipedIdea);
                    SparkToast.show(HomeActivity.this, swipedIdea.isPinned() ? "Idea Pinned" : "Idea Unpinned");
                    if (swipedIdea.isPinned()) {
                        adapter.collapseForPin(swipedIdea.getId());
                    }
                    adapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    float top = itemView.getTop() + 8f;
                    float bottom = itemView.getBottom() - 8f;

                    if (dX < 0) {
                        // Swipe Left (Discard) - only non-pinned items reach here via getSwipeDirs
                        paint.setColor(Color.parseColor("#E8E8E8"));
                        float left = itemView.getRight() + dX;
                        float right = (float) itemView.getRight();
                        RectF rect = new RectF(left, top, right, bottom);
                        c.drawRoundRect(rect, 12f, 12f, paint);

                        if (deleteIcon != null) {
                            int iconSize = 60;
                            int iconMargin = (int) ((bottom - top - iconSize) / 2);
                            int iconTop = (int) top + iconMargin;
                            int iconBottom = iconTop + iconSize;
                            int iconLeft = (int) right - iconMargin - iconSize;
                            int iconRight = iconLeft + iconSize;
                            if (iconLeft > left) {
                                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                deleteIcon.draw(c);
                            }
                        }
                    } else if (dX > 0) {
                        // Swipe Right (Pin/Unpin) - springy with damping
                        float maxSwipe = itemView.getWidth() / 2f;
                        float swipeDx = dX;
                        if (dX > maxSwipe) {
                            swipeDx = maxSwipe + (dX - maxSwipe) * 0.1f;
                        }

                        paint.setColor(Color.parseColor("#E8E8E8"));
                        float left = (float) itemView.getLeft();
                        float right = itemView.getLeft() + swipeDx;
                        RectF rect = new RectF(left, top, right, bottom);
                        c.drawRoundRect(rect, 12f, 12f, paint);

                        if (pinIcon != null) {
                            pinIcon.setTint(Color.parseColor("#555555"));
                            int iconSize = 60;
                            int iconMargin = (int) ((bottom - top - iconSize) / 2);
                            int iconTop = (int) top + iconMargin;
                            int iconBottom = iconTop + iconSize;
                            int iconLeft = (int) left + iconMargin;
                            int iconRight = iconLeft + iconSize;
                            if (iconRight < right) {
                                pinIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                                pinIcon.draw(c);
                            }
                        }
                        
                        super.onChildDraw(c, recyclerView, viewHolder, swipeDx, dY, actionState, isCurrentlyActive);
                        return;
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);


        // FAB Click
        findViewById(R.id.fabQuickCapture).setOnClickListener(v -> {
            QuickCaptureDialog.show(this, idea -> {
                homeViewModel.insert(idea);
            });
        });

        // Discarded Button
        findViewById(R.id.btnDiscarded).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(HomeActivity.this, com.example.spark.ui.discarded.DiscardedActivity.class);
            startActivity(intent);
        });
    }

    private void applyFilter() {
        java.util.List<Idea> filtered = new java.util.ArrayList<>();
        if ("All".equals(currentFilter)) {
            filtered.addAll(allIdeas);
        } else {
            for (Idea idea : allIdeas) {
                if (currentFilter.equals(idea.getStatus())) {
                    filtered.add(idea);
                }
            }
        }
        
        adapter.setIdeas(filtered);
        
        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
