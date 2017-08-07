package org.septa.android.app.support;

import android.content.Context;
import android.database.Cursor;


public interface CursorAdapterSupplier<T> {
    Cursor getCursor(Context context, String whereClause);

    T getCurrentItemFromCursor(Cursor cursor);

    T getItemFromId(Context context, Object id);

}
