package com.example.spark.ui.home;

import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spark.R;
import com.example.spark.data.Idea;
import java.util.ArrayList;
import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder> {

    private List<Idea> ideas = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isGraveyardMode = false;
    private java.util.Set<Integer> expandedIdeas = new java.util.HashSet<>();

    public interface OnItemClickListener {
        void onActionClick(Idea idea);
    }

    public void setGraveyardMode(boolean graveyardMode) {
        this.isGraveyardMode = graveyardMode;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setIdeas(List<Idea> ideas) {
        this.ideas = ideas;
        notifyDataSetChanged();
    }

    /**
     * Ensures the given idea will appear collapsed on next bind.
     * Call this when pinning an idea so it always shrinks.
     */
    public void collapseForPin(int ideaId) {
        expandedIdeas.remove(ideaId);
    }

    public Idea getIdeaAt(int position) {
        return ideas.get(position);
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_idea, parent, false);
        return new IdeaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        Idea currentIdea = ideas.get(position);
        holder.title.setText(currentIdea.getTitle());

        if (isGraveyardMode) {
            holder.status.setVisibility(View.GONE);
            holder.actionBtn.setText("Revive");

            String desc = currentIdea.getDescription();
            holder.preview.setText(desc == null || desc.trim().isEmpty() ? "Not brainstormed yet" : desc);
            holder.expandableContainer.setVisibility(View.VISIBLE);
            // Reset layout params for graveyard mode - no collapsing
            ViewGroup.LayoutParams params = holder.expandableContainer.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.expandableContainer.setLayoutParams(params);
            holder.expandableContainer.setAlpha(1f);
        } else {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(currentIdea.getStatus());
            holder.ivPinIcon.setVisibility(currentIdea.isPinned() ? View.VISIBLE : View.GONE);

            String desc = currentIdea.getDescription();
            if (desc == null || desc.trim().isEmpty()) {
                holder.preview.setText("Not brainstormed yet");
                holder.actionBtn.setText("Brainstorm");
            } else {
                holder.preview.setText(desc);
                holder.actionBtn.setText("Continue");
            }

            boolean isExpanded = expandedIdeas.contains(currentIdea.getId());

            // Set initial state without animation during bind
            if (currentIdea.isPinned() && !isExpanded) {
                holder.expandableContainer.setVisibility(View.GONE);
                holder.expandableContainer.setAlpha(0f);
            } else {
                holder.expandableContainer.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = holder.expandableContainer.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.expandableContainer.setLayoutParams(params);
                holder.expandableContainer.setAlpha(1f);
            }

            holder.itemView.setOnClickListener(v -> {
                if (currentIdea.isPinned()) {
                    boolean wasExpanded = expandedIdeas.contains(currentIdea.getId());
                    if (wasExpanded) {
                        expandedIdeas.remove(currentIdea.getId());
                        animateCollapse(holder.expandableContainer);
                    } else {
                        expandedIdeas.add(currentIdea.getId());
                        animateExpand(holder.expandableContainer);
                    }
                }
            });

            // Spring feedback for left swipe on pinned items
            if (currentIdea.isPinned()) {
                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    private float startX = 0f;
                    private boolean springTriggered = false;

                    @Override
                    public boolean onTouch(View v, android.view.MotionEvent event) {
                        switch (event.getActionMasked()) {
                            case android.view.MotionEvent.ACTION_DOWN:
                                startX = event.getRawX();
                                springTriggered = false;
                                break;
                            case android.view.MotionEvent.ACTION_MOVE:
                                float dx = event.getRawX() - startX;
                                if (dx < -60 && !springTriggered) {
                                    springTriggered = true;
                                    // Spring nudge animation
                                    v.animate().cancel();
                                    v.animate()
                                            .translationX(-80f)
                                            .setDuration(150)
                                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                                            .withEndAction(() -> {
                                                v.animate()
                                                        .translationX(0f)
                                                        .setDuration(300)
                                                        .setInterpolator(new android.view.animation.OvershootInterpolator(2f))
                                                        .start();
                                                android.content.Context ctx = v.getContext();
                                                if (ctx instanceof androidx.appcompat.app.AppCompatActivity) {
                                                    com.example.spark.util.SparkToast.show(
                                                            (androidx.appcompat.app.AppCompatActivity) ctx,
                                                            "Unpin the idea first");
                                                }
                                            })
                                            .start();
                                }
                                break;
                            case android.view.MotionEvent.ACTION_UP:
                            case android.view.MotionEvent.ACTION_CANCEL:
                                startX = 0f;
                                springTriggered = false;
                                break;
                        }
                        // Don't consume - let ItemTouchHelper handle right swipes
                        return false;
                    }
                });
            } else {
                holder.itemView.setOnTouchListener(null);
            }
        }
    }

    private void animateExpand(final View view) {
        // Make it visible but with 0 height to measure
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);

        // Measure the full height
        view.measure(
                View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        final int targetHeight = view.getMeasuredHeight();

        // Start from 0
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = 0;
        view.setLayoutParams(params);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = value;
            view.setLayoutParams(lp);

            // Fade in during the last 60% of the animation
            float fraction = animation.getAnimatedFraction();
            float alpha = Math.max(0f, (fraction - 0.4f) / 0.6f);
            view.setAlpha(alpha);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Switch to wrap_content so the view can adapt if content changes
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(lp);
                view.setAlpha(1f);
            }
        });
        animator.start();
    }

    private void animateCollapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(250);
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.height = value;
            view.setLayoutParams(lp);

            // Fade out during the first 50% of the animation
            float fraction = animation.getAnimatedFraction();
            float alpha = Math.max(0f, 1f - (fraction / 0.5f));
            view.setAlpha(alpha);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
                view.setAlpha(0f);
            }
        });
        animator.start();
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    class IdeaViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView status;
        private TextView preview;
        private Button actionBtn;
        private android.widget.ImageView ivPinIcon;
        private LinearLayout expandableContainer;

        public IdeaViewHolder(View itemView) {
            super(itemView);
            TextView tvTitle = itemView.findViewById(R.id.ideaTitle);
            TextView tvDescriptionPreview = itemView.findViewById(R.id.ideaDescriptionPreview);
            TextView tvStatus = itemView.findViewById(R.id.statusTag);
            android.widget.ImageView ivPinIconLocal = itemView.findViewById(R.id.ivPinIcon);
            com.google.android.material.button.MaterialButton btnAction = itemView.findViewById(R.id.btnAction);
            LinearLayout expandableContainerLocal = itemView.findViewById(R.id.expandableContainer);

            this.title = tvTitle;
            this.preview = tvDescriptionPreview;
            this.status = tvStatus;
            this.actionBtn = btnAction;
            this.ivPinIcon = ivPinIconLocal;
            this.expandableContainer = expandableContainerLocal;

            actionBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onActionClick(ideas.get(position));
                }
            });
        }
    }
}
