package com.frederikam.piprint.svg.geom;

public class StraightLine extends Line {

    private final Point start;
    private final Point end;
    private final Point diff;

    public StraightLine(Point start, Point end) {
        this.start = start;
        this.end = end;
        this.diff = end.minus(start);
    }

    @Override
    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "StraightLine{" +
                start +
                "->" + end +
                '}';
    }

    @Override
    public Point tween(double t) {
        return start.plus(diff.multiply(t));
    }
}
