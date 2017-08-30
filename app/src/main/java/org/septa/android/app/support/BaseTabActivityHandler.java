package org.septa.android.app.support;

/**
 * Created by jkampf on 7/29/17.
 */

public abstract class BaseTabActivityHandler implements TabActivityHandler {

    private String title;
    private Integer inactiveIconDrawable;
    private Integer activeIconDrawable;

    public BaseTabActivityHandler(String title) {
        this.title = title;
    }

    public BaseTabActivityHandler(String title, Integer inactiveIconDrawable, Integer activeIconDrawable) {
        this.title = title;
        this.inactiveIconDrawable = inactiveIconDrawable;
        this.activeIconDrawable = activeIconDrawable;
    }


    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public Integer getInactiveDrawableId() {
        return inactiveIconDrawable;
    }

    @Override
    public Integer getActiveDrawableId() {
        return activeIconDrawable;
    }

}
