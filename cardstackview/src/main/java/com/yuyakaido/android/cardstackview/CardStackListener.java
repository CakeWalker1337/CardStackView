package com.yuyakaido.android.cardstackview;

import android.view.View;

public interface CardStackListener {
    void onCardDragging(Direction directionX, Direction directionY, float ratioX, float ratioY);
    void onCardSwiped(Direction direction);
    void onCardRewound();
    void onCardCanceled();
    void onCardAppeared(View view, int position);
    void onCardDisappeared(View view, int position);

    CardStackListener DEFAULT = new CardStackListener() {
        @Override
        public void onCardDragging(Direction directionX, Direction directionY, float ratioX, float ratioY) {}
        @Override
        public void onCardSwiped(Direction direction) {}
        @Override
        public void onCardRewound() {}
        @Override
        public void onCardCanceled() {}
        @Override
        public void onCardAppeared(View view, int position) {}
        @Override
        public void onCardDisappeared(View view, int position) {}
    };
}
