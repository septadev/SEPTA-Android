package org.septa.android.app.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Html;
import android.util.AttributeSet;

import org.septa.android.app.managers.FontManager;

public class EditText extends AppCompatEditText {
    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        FontManager.getInstance().setFont(this, attrs);

        if (this.getText() != null) {
            this.setText(Html.fromHtml(this.getText().toString()));
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
