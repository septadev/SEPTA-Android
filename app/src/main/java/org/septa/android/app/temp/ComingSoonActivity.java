package org.septa.android.app.temp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;

import org.septa.android.app.R;

/**
 * Created by jkampf on 9/12/17.
 */

public class ComingSoonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coming_soon);

        setTitle("Coming Soon!");
    }
}
