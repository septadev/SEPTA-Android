package org.septa.android.app.support;

import android.support.v4.app.Fragment;

/**
 * Created by jkampf on 7/29/17.
 */

public interface TabActivityHandler {

    String getTabTitle();

    Fragment getFragment();

    Integer getInactiveDrawableId();

    Integer getActiveDrawableId();
}
