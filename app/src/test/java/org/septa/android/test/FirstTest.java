package org.septa.android.test;

/**
 * Created by bmayo on 5/28/14.
 */
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;
import org.septa.android.app.R;
import org.septa.android.app.activities.FareInformationActionBarActivity;
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

        FragmentActivity ac = Robolectric.buildActivity(FareInformationActionBarActivity.class).create().get();
        Assert.assertTrue(ac != null);

        FragmentManager fm = ac.getSupportFragmentManager();

        ListFragment list = (ListFragment)fm.findFragmentById(R.id.fareInformation_listView_fragment);
        Assert.assertTrue(list != null);

        // the fare information listview should have 8 rows, 7 for the fare information and the "get more information" button
        Assert.assertEquals(8, list.getListAdapter().getCount());
    }
}
