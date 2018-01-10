package com.frederikam.piprint.svg.geom;

import java.awt.*;

public abstract class Line {

    private Color color = Color.BLACK;

    public abstract Point tween(double t);

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
