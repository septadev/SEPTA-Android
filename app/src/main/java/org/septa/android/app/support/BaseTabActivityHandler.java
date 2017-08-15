package org.septa.android.app.support;

/**
 * Created by jkampf on 7/29/17.
 */

public abstract class BaseTabActivityHandler implements TabActivityHandler {

    private String title;
    private Integer iconDrawable;

    public BaseTabActivityHandler(String title){
        this.title = title;
    }

    public BaseTabActivityHandler(String title, Integer iconDrawable){
        this.title = title;
        this.iconDrawable = iconDrawable;
    }


    @Override
    public String getTabTitle() {
        return title;
    }

    @Override
    public Integer getDrawableId() {
        return iconDrawable;
    }
}
