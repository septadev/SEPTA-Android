package org.septa.android.app.support;

import android.content.Context;
import android.database.Cursor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface CursorAdapterSupplier<T> extends Serializable {
    Cursor getCursor(Context context, List<Criteria> whereClause);

    T getCurrentItemFromCursor(Cursor cursor);

    T getItemFromId(Context context, Object id);


}
