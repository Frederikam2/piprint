package com.frederikam.piprint.svg.geom;

/**
 * A double precision point
 */
public class Point {

    private double x;
    private double y;

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

    @Override
    public String toString() {
        return "(" + x + "," + y + ')';
    }
}
