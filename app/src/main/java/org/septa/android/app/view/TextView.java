package org.septa.android.app.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

import org.septa.android.app.managers.FontManager;

public class TextView extends AppCompatTextView {

    private static final String NEW_LINE_CHARACTER = "\n", HTML_NEW_LINE_CHARACTER = "<br/>";

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        FontManager.getInstance().setFont(this, attrs);

        if (this.getText() != null) {
            this.setText(Html.fromHtml(this.getText().toString() // enable HTML support
                    .replace(NEW_LINE_CHARACTER, HTML_NEW_LINE_CHARACTER))); // new line character support
        }
    }

    public void setFont(String fontPath) {
        FontManager.getInstance().setFont(this, fontPath);
    }

    public void setFont(int resId) {
        String fontPath = getContext().getString(resId);
        setFont(fontPath);
    }

    public void setHtml(String html) {
        this.setText(Html.fromHtml(html));
    }

}
