package org.septa.android.app.support;

import android.support.v4.app.Fragment;

public interface TabActivityHandler {

    String getTabTitle();

    Fragment getFragment();

    Integer getInactiveDrawableId();

    Integer getActiveDrawableId();
}
