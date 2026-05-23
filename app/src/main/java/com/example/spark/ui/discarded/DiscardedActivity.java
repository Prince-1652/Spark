package com.example.spark.ui.discarded;

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
import com.example.spark.ui.home.IdeaAdapter;
import com.example.spark.util.SparkToast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DiscardedActivity extends AppCompatActivity {

    private DiscardedViewModel discardedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discarded);

        findViewById(R.id.btnBackFromDiscarded).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.discardedRecyclerView);
        TextView emptyState = findViewById(R.id.emptyDiscardedText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // recyclerView.setHasFixedSize(true); Removed due to wrap_content constraint

        IdeaAdapter adapter = new IdeaAdapter();
        adapter.setGraveyardMode(true);
        recyclerView.setAdapter(adapter);

        discardedViewModel = new ViewModelProvider(this).get(DiscardedViewModel.class);
        discardedViewModel.getDiscardedIdeas().observe(this, ideas -> {
            adapter.setIdeas(ideas);
            if (ideas == null || ideas.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        adapter.setOnItemClickListener(idea -> {
            idea.setStatus(idea.getDescription() == null || idea.getDescription().trim().isEmpty() ? "Spark" : "Brainstorming");
            idea.setLastUpdated(System.currentTimeMillis());
            discardedViewModel.update(idea);
            SparkToast.show(this, "Idea revived!");
        });

        // Swipe to permanently delete with confirmation
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Paint paint = new Paint();
            private final android.graphics.drawable.Drawable deleteIcon = ContextCompat.getDrawable(DiscardedActivity.this, R.drawable.ic_delete_grey);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Idea swipedIdea = adapter.getIdeaAt(position);

                DeleteConfirmDialog.show(DiscardedActivity.this, swipedIdea,
                        idea -> {
                            discardedViewModel.delete(idea);
                            SparkToast.show(DiscardedActivity.this, "Idea permanently deleted");
                        },
                        () -> {
                            adapter.notifyItemChanged(position);
                        }
                );
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;
                    paint.setColor(Color.parseColor("#E8E8E8"));

                    float top = itemView.getTop() + 8f;
                    float bottom = itemView.getBottom() - 8f;
                    float left = itemView.getRight() + dX;
                    float right = (float) itemView.getRight();

                    RectF rect = new RectF(left, top, right, bottom);
                    c.drawRoundRect(rect, 12f, 12f, paint);

                    // Draw delete icon
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
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }
}
