package com.frederikam.piprint.svg.geom;

/**
 * A double precision point
 */
public class Point {

    double x;
    double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Point plus(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public Point minus(Point other) {
        return new Point(x - other.x, y - other.y);
    }

    public Point multiply(double factor) {
        return new Point(x * factor, y * factor);
    }

    public Point divide(double dividend) {
        return new Point(x / dividend, y / dividend);
    }

    public double magnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public Point unit() {
        return divide(magnitude());
    }

    public Point scaleToHaveOneAxisBe1() {
        if (x == 0 && y == 0) return this;

        boolean isXLargestAxis = Math.abs(x) > Math.abs(y);
        double factor = Math.abs(isXLargestAxis ? 1 / x : 1 / y);
        return multiply(factor);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ')';
    }
}
