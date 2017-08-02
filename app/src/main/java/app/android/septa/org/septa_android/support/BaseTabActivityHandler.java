package app.android.septa.org.septa_android.support;

/**
 * Created by jkampf on 7/29/17.
 */

public abstract class BaseTabActivityHandler implements TabActivityHandler {

    private String title;

    public BaseTabActivityHandler(String title){
        this.title = title;
    }


    @Override
    public String getTabTitle() {
        return title;
    }

 }
