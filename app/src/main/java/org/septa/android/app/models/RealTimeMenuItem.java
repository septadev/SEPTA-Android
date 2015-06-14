package org.septa.android.app.models;

/**
 * Created by jhunchar on 4/17/15.
 */
public class RealTimeMenuItem {

    private final Class classname;
    private final String icon;
    private final String selectableIcon;
    private final String title;

    public RealTimeMenuItem(Class classname, String icon, String selectableIcon, String title) {
        super();
        this.classname = classname;
        this.icon = icon;
        this.selectableIcon = selectableIcon;
        this.title = title;
    }

    /**
     * @return the classname
     */
    public Class getClassname() {
        return classname;
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @return the selectable icon
     */
    public String getSelectableIcon() {
        return selectableIcon;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
}

