package org.septa.android.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.septa.android.app.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Trey Robinson on 8/1/14.
 *
 * Displays a spinner when loading and an empty view when isLoading is false.
 * 
 */
public class StatusView extends RelativeLayout {

    @InjectView(R.id.empty_textview)
    TextView emptyText;

    @InjectView(R.id.loading_view)
    View loadingView;

    private boolean isLoading;

    public StatusView(Context context) {
        super(context);
        initViews(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.status_view, this, true);
        ButterKnife.inject(this);
    }

    public void setLoading(boolean loading){
        isLoading = loading;
        if(isLoading){
            loadingView.setVisibility(VISIBLE);
            emptyText.setVisibility(INVISIBLE);
        } else {
            loadingView.setVisibility(INVISIBLE);
            emptyText.setVisibility(VISIBLE);
        }
    }

    public boolean isLoading(){
        return isLoading;
    }
}
