/*
 * SelectiveNonSwipingViewPager.java
 * Last modified on 02-03-2014 13:57-0500 by brianhmayo
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Iterator;

public class SelectiveNonSwipingViewPager extends ViewPager {
    private static final String TAG = SelectiveNonSwipingViewPager.class.getName();

    private final String[] nonswipeableViewPagerMainItems =
            getResources().getStringArray(R.array.nonswipeable_viewpager_main_items);

    public SelectiveNonSwipingViewPager(Context context) {
        super(context);
    }

    public SelectiveNonSwipingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        for (String nonswipeableViewPagerMainItem: nonswipeableViewPagerMainItems) {
            if (nonswipeableViewPagerMainItem.equals(getResources().getStringArray(R.array.nav_main_items)[(this.getCurrentItem())])) {
                return false;
            }
        }

        return super.onInterceptTouchEvent(arg0);
    }
}
