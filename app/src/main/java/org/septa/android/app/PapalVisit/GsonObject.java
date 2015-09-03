package org.septa.android.app.PapalVisit;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.septa.android.app.BuildConfig;

/**
 * Added by jhunchar on 9/3/15.
 */
public class GsonObject {
    private static final String TAG = GsonObject.class.getName();

    @Override
    public String toString() {
        return toJsonString(true);
    }

    public static <T> T fromJson(String jsonString, Class<T> classType) {
        if (jsonString != null) {
            try {
                return new Gson().fromJson(jsonString, classType);
            }
            catch (JsonSyntaxException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "fromJson: invalid json entered", e);
                }
            }
        }
        return null;
    }

    public static String convertObjectToJsonString(Object objectToConvert, boolean prettyPrint) {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .serializeNulls();

        if (prettyPrint) {
            gsonBuilder.setPrettyPrinting();
        }

        return gsonBuilder.create().toJson(objectToConvert);
    }

    public static String convertObjectToJson(Object objectToConvert) {
        return convertObjectToJsonString(objectToConvert, false);
    }

    public String toJsonString(boolean prettyPrint) {
        return convertObjectToJsonString(this, prettyPrint);
    }
}

