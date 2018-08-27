package org.septa.android.app.view;

import android.graphics.PointF;

import java.io.Serializable;

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
public class ImageViewState implements Serializable {

    private float scale;

    private float centerX;

    private float centerY;

    private int orientation;

    ImageViewState(float scale, PointF center, int orientation) {
        this.scale = scale;
        this.centerX = center.x;
        this.centerY = center.y;
        this.orientation = orientation;
    }

    public float getScale() {
        return scale;
    }

    public PointF getCenter() {
        return new PointF(centerX, centerY);
    }

    public int getOrientation() {
        return orientation;
    }

}