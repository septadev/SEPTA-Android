package org.septa.android.app.favorites;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FavoriteState implements Serializable {

    @SerializedName("favoriteKey")
    private String favoriteKey;

    @SerializedName("isExpanded")
    private boolean isExpanded;

    public FavoriteState(String favoriteKey) {
        this.favoriteKey = favoriteKey;
        this.isExpanded = false;
    }

    public FavoriteState(String favoriteKey, boolean isExpanded) {
        this.favoriteKey = favoriteKey;
        this.isExpanded = isExpanded;
    }

    public String getFavoriteKey() {
        return favoriteKey;
    }

    public void setFavoriteKey(String favoriteKey) {
        this.favoriteKey = favoriteKey;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FavoriteState that = (FavoriteState) o;

        if (isExpanded != that.isExpanded) {
            return false;
        }
        return favoriteKey.equals(that.favoriteKey);
    }

    @Override
    public int hashCode() {
        int result = favoriteKey.hashCode();
        result = 31 * result + (isExpanded ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FavoriteState{" +
                "favoriteKey='" + favoriteKey + '\'' +
                ", isExpanded=" + isExpanded +
                '}';
    }
}
