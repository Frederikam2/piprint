package com.frederikam.piprint.svg.geom;

// https://www.geogebra.org/m/WPHQ9rUt
public class CubicBezierCurve extends Line {

    private final Point p1;
    private final Point p2;
    private final Point p3;
    private final Point p4;

    public CubicBezierCurve(Point p1, Point p2, Point p3, Point p4) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.p4 = p4;
    }

    @Override
    public Point tween(double t) {
        double t2 = 1 - t;

        Point p5 = p1.multiply(t2).plus(p2.multiply(t));
        Point p6 = p2.multiply(t2).plus(p3.multiply(t));
        Point p7 = p3.multiply(t2).plus(p4.multiply(t));
        Point p8 = p5.multiply(t2).plus(p6.multiply(t));
        Point p9 = p6.multiply(t2).plus(p7.multiply(t));
        return     p8.multiply(t2).plus(p9.multiply(t));
    }

    public double estimateLength() {
        // Too complicated to do exactly. This uses tweening instead
        // https://math.stackexchange.com/questions/12186/arc-length-of-b%C3%A9zier-curves

        double sum = 0;
        Point lastPoint = p1;
        for (double i = 0; i < 100; i++) {
            Point newP = tween(i / 100);
            sum += newP.minus(lastPoint).magnitude();
            lastPoint = newP;
        }

        return sum;
    }

    @Override
    public Point getStart() {
        return p1;
    }

    @Override
    public String toString() {
        return "CubicBezierCurve{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", p3=" + p3 +
                ", p4=" + p4 +
                '}';
    }
}
