package org.septa.android.app.services.apiinterfaces.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public abstract class Favorite implements Serializable {

    public static final String FAVORITE_KEY_DELIM = "_";

    @SerializedName("name")
    protected String name;

    @SerializedName("created_with_version")
    int createdWithVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreatedWithVersion() {
        return createdWithVersion;
    }

    public abstract String getKey();

}