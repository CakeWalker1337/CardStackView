package com.yuyakaido.android.cardstackview;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.yuyakaido.android.cardstackview.internal.CardStackSetting;
import com.yuyakaido.android.cardstackview.internal.CardStackSmoothScroller;
import com.yuyakaido.android.cardstackview.internal.CardStackState;

import java.util.List;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CardStackLayoutManager
        extends RecyclerView.LayoutManager
        implements RecyclerView.SmoothScroller.ScrollVectorProvider{

    private final Context context;

    private CardStackListener listener = CardStackListener.DEFAULT;
    private CardStackSetting setting = new CardStackSetting();
    private CardStackState state = new CardStackState();

    public CardStackLayoutManager(Context context){
        this(context, CardStackListener.DEFAULT);
    }

    public CardStackLayoutManager(Context context, CardStackListener listener){
        this.context = context;
        this.listener = listener;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams(){
        return new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State s){
        initItem(recycler, state.lastPosition);
    }

    public View initItem(RecyclerView.Recycler recycler, int position){
        if(position - state.topPosition < setting.visibleCount){
            final int parentTop = getPaddingTop();
            final int parentLeft = getPaddingLeft();
            final int parentRight = getWidth() - getPaddingLeft();
            final int parentBottom = getHeight() - getPaddingBottom();
            View child = recycler.getViewForPosition(state.lastPosition);
            if(state.lastPosition == state.topPosition){
                updateScale(child);
            } else{
                resetScale(child);
            }
            resetOverlay(child);
            addView(child, 0);
            measureChildWithMargins(child, 0, 0);
            layoutDecoratedWithMargins(child, parentLeft, parentTop, parentRight, parentBottom);
            state.lastPosition++;
            View topView = getTopView();
            listener.onCardAppeared(topView, state.topPosition);
            return topView;
        }
        return null;
    }

    @Override
    public boolean canScrollHorizontally(){
        return setting.swipeableMethod.canSwipe() && setting.canScrollHorizontal;
    }

    @Override
    public boolean canScrollVertically(){
        return setting.swipeableMethod.canSwipe() && setting.canScrollVertical;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State s){
        if(state.topPosition == getItemCount()){
            return 0;
        }

        switch(state.status){
            case Idle:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dx -= dx;
                    update(recycler);
                    return dx;
                }
                break;
            case Dragging:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dx -= dx;
                    update(recycler);
                    return dx;
                }
                break;
            case RewindAnimating:
                state.dx -= dx;
                update(recycler);
                return dx;
            case AutomaticSwipeAnimating:
                if(setting.swipeableMethod.canSwipeAutomatically()){
                    state.dx -= dx;
                    update(recycler);
                    return dx;
                }
                break;
            case AutomaticSwipeAnimated:
                break;
            case ManualSwipeAnimating:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dx -= dx;
                    update(recycler);
                    return dx;
                }
                break;
            case ManualSwipeAnimated:
                break;
        }

        return 0;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State s){
        if(state.topPosition == getItemCount()){
            return 0;
        }

        switch(state.status){
            case Idle:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dy -= dy;
                    update(recycler);
                    return dy;
                }
                break;
            case Dragging:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dy -= dy;
                    update(recycler);
                    return dy;
                }
                break;
            case RewindAnimating:
                state.dy -= dy;
                update(recycler);
                return dy;
            case AutomaticSwipeAnimating:
                if(setting.swipeableMethod.canSwipeAutomatically()){
                    state.dy -= dy;
                    update(recycler);
                    return dy;
                }
                break;
            case AutomaticSwipeAnimated:
                break;
            case ManualSwipeAnimating:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.dy -= dy;
                    update(recycler);
                    return dy;
                }
                break;
            case ManualSwipeAnimated:
                break;
        }
        return 0;
    }

    @Override
    public void onScrollStateChanged(int s){
        switch(s){
            // スクロールが止まったタイミング
            case RecyclerView.SCROLL_STATE_IDLE:
                if(state.targetPosition == RecyclerView.NO_POSITION){
                    // Swipeが完了した場合の処理
                    state.next(CardStackState.Status.Idle);
                    state.targetPosition = RecyclerView.NO_POSITION;
                } else if(state.topPosition == state.targetPosition){
                    // Rewindが完了した場合の処理
                    state.next(CardStackState.Status.Idle);
                    state.targetPosition = RecyclerView.NO_POSITION;
                } else{
                    // 2枚以上のカードを同時にスワイプする場合の処理
                    if(state.topPosition < state.targetPosition){
                        // 1枚目のカードをスワイプすると一旦SCROLL_STATE_IDLEが流れる
                        // そのタイミングで次のアニメーションを走らせることで連続でスワイプしているように見せる
                        smoothScrollToNext(state.targetPosition);
                    } else{
                        // Nextの場合と同様に、1枚目の処理が完了したタイミングで次のアニメーションを走らせる
                        smoothScrollToPrevious(state.targetPosition);
                    }
                }
                break;
            // カードをドラッグしている最中
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if(setting.swipeableMethod.canSwipeManually()){
                    state.next(CardStackState.Status.Dragging);
                }
                break;
            // カードが指から離れたタイミング
            case RecyclerView.SCROLL_STATE_SETTLING:
                break;
        }
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition){
        return null;
    }

    @Override
    public void scrollToPosition(int position){
        if(setting.swipeableMethod.canSwipeAutomatically()){
            if(state.canScrollToPosition(position, getItemCount())){
                state.topPosition = position;
                requestLayout();
            }
        }
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State s, int position){
        if(setting.swipeableMethod.canSwipeAutomatically()){
            if(state.canScrollToPosition(position, getItemCount())){
                smoothScrollToPosition(position);
            }
        }
    }

    @NonNull
    public CardStackSetting getCardStackSetting(){
        return setting;
    }

    @NonNull
    public CardStackState getCardStackState(){
        return state;
    }

    @NonNull
    public CardStackListener getCardStackListener(){
        return listener;
    }

    void updateProportion(float x, float y){
        if(getTopPosition() < getItemCount()){
            View view = findViewByPosition(getTopPosition());
            if(view != null){
                float half = getHeight() / 2.0f;
                state.proportion = -(y - half - view.getTop()) / half;
            }
        }
    }

    private void update(RecyclerView.Recycler recycler){
        state.width = getWidth();
        state.height = getHeight();

        View topView = getTopView();
        if(state.isSwipeCompleted()){
            resetTranslation(topView);
            resetScale(topView);
            resetRotation(topView);
            resetOverlay(topView);
            removeAndRecycleView(topView, recycler);

            final Direction direction = state.getDirection();

            state.next(state.status.toAnimatedStatus());
            state.topPosition++;

            state.dx = 0;
            state.dy = 0;
            if(state.topPosition == state.targetPosition){
                state.targetPosition = RecyclerView.NO_POSITION;
                initItem(recycler, state.lastPosition);
                updateScale(getTopView());
            }
            new Handler().post(new Runnable(){

                @Override
                public void run(){
                    listener.onCardSwiped(direction);
                    View topView = getTopView();
                    if(topView != null){
                        listener.onCardAppeared(getTopView(), state.topPosition);
                    }
                }
            });
        } else{

            updateTranslation(topView);
            updateRotation(topView);
            updateOverlay(topView);

            if(state.status.isDragging()){
                listener.onCardDragging(state.getDirectionX(), state.getDirectionY(), state.getRatioX(), state.getRatioY());
            }
        }
    }

    private void updateTranslation(View view){
        view.setTranslationX(state.dx);
        view.setTranslationY(state.dy);
    }

    private void resetTranslation(View view){
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
    }

    private void updateScale(View view){
        view.animate().scaleX(1.0F).scaleY(1.0F).setDuration(200L).start();
    }

    private void resetScale(View view){
        view.setScaleX(0.9f);
        view.setScaleY(0.9f);
    }

    private void updateRotation(View view){
        float degree = state.dx * setting.maxDegree / getWidth() * state.proportion;
        view.setRotation(degree);
    }

    private void resetRotation(View view){
        view.setRotation(0.0f);
    }

    private void updateOverlay(View view){
        View leftOverlay = view.findViewById(R.id.left_overlay);
        if(leftOverlay != null){
            leftOverlay.setAlpha(0.0f);
        }
        View rightOverlay = view.findViewById(R.id.right_overlay);
        if(rightOverlay != null){
            rightOverlay.setAlpha(0.0f);
        }
        View topOverlay = view.findViewById(R.id.top_overlay);
        if(topOverlay != null){
            topOverlay.setAlpha(0.0f);
        }
        View bottomOverlay = view.findViewById(R.id.bottom_overlay);
        if(bottomOverlay != null){
            bottomOverlay.setAlpha(0.0f);
        }
        Direction directionX = state.getDirectionX();
        Direction directionY = state.getDirectionY();
        float alphaX = setting.overlayInterpolator.getInterpolation(state.getRatioX());
        float alphaY = setting.overlayInterpolator.getInterpolation(state.getRatioY());
        float restrictionVector = (float) Math.hypot(alphaX, alphaY);
        float reduceCoefficient = 1.0F;
        if(restrictionVector > 1.0F){
            reduceCoefficient = 1.0F / restrictionVector;
        }
        switch(directionX){
            case Left:
                if(leftOverlay != null){
                    leftOverlay.setAlpha(alphaX * reduceCoefficient);
                }
                break;
            case Right:
                if(rightOverlay != null){
                    rightOverlay.setAlpha(alphaX * reduceCoefficient);
                }
                break;
        }
        switch(directionY){
            case Top:
                if(topOverlay != null){
                    topOverlay.setAlpha(alphaY * reduceCoefficient);
                }
                break;
            case Bottom:
                if(bottomOverlay != null){
                    bottomOverlay.setAlpha(alphaY * reduceCoefficient);
                }
                break;
        }
    }

    private void resetOverlay(View view){
        View leftOverlay = view.findViewById(R.id.left_overlay);
        if(leftOverlay != null){
            leftOverlay.setAlpha(0.0f);
        }
        View rightOverlay = view.findViewById(R.id.right_overlay);
        if(rightOverlay != null){
            rightOverlay.setAlpha(0.0f);
        }
        View topOverlay = view.findViewById(R.id.top_overlay);
        if(topOverlay != null){
            topOverlay.setAlpha(0.0f);
        }
        View bottomOverlay = view.findViewById(R.id.bottom_overlay);
        if(bottomOverlay != null){
            bottomOverlay.setAlpha(0.0f);
        }
    }

    private void smoothScrollToPosition(int position){
        if(state.topPosition < position){
            smoothScrollToNext(position);
        } else{
            smoothScrollToPrevious(position);
        }
    }

    private void smoothScrollToNext(int position){
        state.proportion = 0.0f;
        state.targetPosition = position;
        CardStackSmoothScroller scroller = new CardStackSmoothScroller(CardStackSmoothScroller.ScrollType.AutomaticSwipe, this);
        scroller.setTargetPosition(state.topPosition);
        startSmoothScroll(scroller);
    }

    private void smoothScrollToPrevious(int position){
        View topView = getTopView();
        if(topView != null){
            listener.onCardDisappeared(getTopView(), state.topPosition);
        }

        state.proportion = 0.0f;
        state.targetPosition = position;
        state.topPosition--;
        state.lastPosition--;
        CardStackSmoothScroller scroller = new CardStackSmoothScroller(CardStackSmoothScroller.ScrollType.AutomaticRewind, this);
        scroller.setTargetPosition(state.topPosition);
        startSmoothScroll(scroller);
    }

    public View getTopView(){
        return findViewByPosition(state.topPosition);
    }

    public int getTopPosition(){
        return state.topPosition;
    }

    public void setTopPosition(int topPosition){
        state.topPosition = topPosition;
    }

    public void setVisibleCount(@IntRange(from = 1) int visibleCount){
        if(visibleCount < 1){
            throw new IllegalArgumentException("VisibleCount must be greater than 0.");
        }
        setting.visibleCount = visibleCount;
    }

    public void setTranslationInterval(@FloatRange(from = 0.0f) float translationInterval){
        if(translationInterval < 0.0f){
            throw new IllegalArgumentException("TranslationInterval must be greater than or equal 0.0f");
        }
        setting.translationInterval = translationInterval;
    }

    public void setScaleInterval(@FloatRange(from = 0.0f) float scaleInterval){
        if(scaleInterval < 0.0f){
            throw new IllegalArgumentException("ScaleInterval must be greater than or equal 0.0f.");
        }
        setting.scaleInterval = scaleInterval;
    }

    public void setSwipeThreshold(@FloatRange(from = 0.0f, to = 1.0f) float swipeThreshold){
        if(swipeThreshold < 0.0f || 1.0f < swipeThreshold){
            throw new IllegalArgumentException("SwipeThreshold must be 0.0f to 1.0f.");
        }
        setting.swipeThreshold = swipeThreshold;
    }

    public void setMaxDegree(@FloatRange(from = -360.0f, to = 360.0f) float maxDegree){
        if(maxDegree < -360.0f || 360.0f < maxDegree){
            throw new IllegalArgumentException("MaxDegree must be -360.0f to 360.0f");
        }
        setting.maxDegree = maxDegree;
    }

    public void setDirections(@NonNull List<Direction> directions){
        setting.directions = directions;
    }

    public void setCanScrollHorizontal(boolean canScrollHorizontal){
        setting.canScrollHorizontal = canScrollHorizontal;
    }

    public void setCanScrollVertical(boolean canScrollVertical){
        setting.canScrollVertical = canScrollVertical;
    }

    public void setSwipeableMethod(SwipeableMethod swipeableMethod){
        setting.swipeableMethod = swipeableMethod;
    }

    public void setSwipeAnimationSetting(@NonNull SwipeAnimationSetting swipeAnimationSetting){
        setting.swipeAnimationSetting = swipeAnimationSetting;
    }

    public void setRewindAnimationSetting(@NonNull RewindAnimationSetting rewindAnimationSetting){
        setting.rewindAnimationSetting = rewindAnimationSetting;
    }

    public void setOverlayInterpolator(@NonNull Interpolator overlayInterpolator){
        setting.overlayInterpolator = overlayInterpolator;
    }

    public void setBaseCardElevation(@NonNull float elevation){
        setting.baseCardElevation = elevation;
    }

}
