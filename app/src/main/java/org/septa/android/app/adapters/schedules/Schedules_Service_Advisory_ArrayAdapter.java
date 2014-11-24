/**
 * Created by acampbell on 11/3/14.
 *
 * Copyright (c) 2014 SEPTA.  All rights reserved.
 */

package org.septa.android.app.adapters.schedules;

import android.content.Context;
import android.widget.ArrayAdapter;

import org.septa.android.app.models.servicemodels.ServiceAdvisoryModel;

import java.util.List;

public class Schedules_Service_Advisory_ArrayAdapter extends ArrayAdapter<ServiceAdvisoryModel> {

    public Schedules_Service_Advisory_ArrayAdapter(Context context, int resource, List<ServiceAdvisoryModel> objects) {
        super(context, resource, objects);

    }

    public void test() {

    }


}
