package com.frederikam.piprint.svg.geom;

public class StraightLine implements Line {

    private final Point start;
    private final Point end;

    public StraightLine(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

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
}
