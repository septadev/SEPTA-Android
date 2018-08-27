package org.septa.android.app;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.septa.android.app.rating.SharedPreferencesRatingUtil;

public class BaseActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onPause() {
        // only call if this is the last activity on the stack
        if (isTaskRoot()) {
            // save that app did not crash
            SharedPreferencesRatingUtil.setAppJustCrashed(BaseActivity.this, false);

            Log.d(TAG, "The app did NOT just crash");
        }

        super.onPause();
    }
}
