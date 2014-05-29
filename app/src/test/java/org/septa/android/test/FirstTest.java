package org.septa.android.test;

/**
 * Created by bmayo on 5/28/14.
 */
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.septa.android.app.R;
import org.septa.android.app.activities.MainTabbarActivity;

/**
 * Created by vandekr on 11/02/14.
 */
@Config(emulateSdk = 18) //Robolectric support API level 18,17, 16, but not 19
@RunWith(RobolectricTestRunner.class)
public class FirstTest {
    @Before
    public void setup() {
        //do whatever is necessary before every test
    }

    @Test
    public void testShouldFail() {
        Activity activity = Robolectric.buildActivity(MainTabbarActivity.class).create().get();
        Assert.assertTrue(activity != null);
    }
}
