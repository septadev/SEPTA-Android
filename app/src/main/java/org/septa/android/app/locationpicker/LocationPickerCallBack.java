package org.septa.android.app.locationpicker;

import org.septa.android.app.domain.StopModel;

/**
 * Created by jkampf on 10/5/17.
 */

public interface LocationPickerCallBack {
    void setLocation(StopModel stopModel);
}
