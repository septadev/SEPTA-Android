package org.septa.android.app.favorites;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

public class FavoritesSwipeRefreshLayout extends SwipeRefreshLayout {
    private View mScrollingView;

    public FavoritesSwipeRefreshLayout(Context context) {
        super(context);
    }

    public FavoritesSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return mScrollingView != null && mScrollingView.canScrollVertically( -1);
    }

    public void setScrollingView(View scrollingView) {
        mScrollingView = scrollingView;
    }
}
