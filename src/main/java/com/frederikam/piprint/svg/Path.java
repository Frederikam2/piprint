package com.frederikam.piprint.svg;

import com.frederikam.piprint.svg.geom.CubicBezierCurve;
import com.frederikam.piprint.svg.geom.Line;
import com.frederikam.piprint.svg.geom.Point;
import com.frederikam.piprint.svg.geom.StraightLine;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An SVG path
 * <p>
 * More info: https://www.w3.org/TR/SVG2/paths.html
 */
public class Path {

    /**
     * Matches the command and a list of numbers to go with those commands
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static Pattern cmdPattern = Pattern.compile("([a-z])([^a-z]*)", Pattern.CASE_INSENSITIVE);
    private Command lastCommand = null;
    private Point positionBeforeLastCommand = null;
    private Point currentPos = new Point(0, 0);
    private List<Line> lines = new LinkedList<>();


    /**
     * Used by the z command
     */
    private LinkedList<Point> subpathStartPoints = new LinkedList<>();

    Path(Node node) {
        String d = node.getAttributes().getNamedItem("d").getTextContent();
        Matcher matcher = cmdPattern.matcher(d);

        List<Command> commands = new LinkedList<>();

        while (matcher.find()) {
            char cmd = matcher.group(1).charAt(0);
            /*List<Double> argList = new LinkedList<>();
            Scanner scanner = new Scanner(matcher.group(2));
            while (scanner.hasNextDouble()) argList.add(scanner.nextDouble());*/
            commands.add(new Command(cmd, matcher.group(2)));
        }

        for (Command cmd : commands) {
            switch (Character.toLowerCase(cmd.command)) {
                case 'm':
                    cmdMoveTo(cmd);
                    break;
                case 'z':
                    cmdEndPath(cmd);
                    break;
                case 'l':
                    cmdLineTo(cmd);
                    break;
                case 'h':
                    cmdLineToHorizontal(cmd);
                    break;
                case 'v':
                    cmdLineToVertical(cmd);
                    break;
                case 'c':
                    cmdLineToCubicBezier(cmd);
                    break;
                case 's':
                    cmdLineToCubicBezierShorthand(cmd);
                    break;
                default:
                    System.out.println("Unknown command: " + cmd.toString());
            }
            lastCommand = cmd;
        }
    }

    /* moveto */

    private void cmdMoveTo(Command cmd) {
        if (cmd.isRelative()) {
            currentPos = currentPos.plus(
                    new Point(cmd.args().get(0), cmd.args().get(1))
            );
        } else {
            currentPos = new Point(cmd.args().get(0), cmd.args().get(1));
        }

        subpathStartPoints.add(currentPos);

        // Extra x/y pairs will be treated as lineto args as per the spec
        if (cmd.args().size() > 2) {
            cmdLineTo(new Command(
                    cmd.isRelative() ? 'l' : 'L',
                    cmd.args().subList(2, cmd.args().size()))
            );
        }
    }

    /* closepath */

    private void cmdEndPath(Command cmd) {
        /*
        todo from the spec:
        If a "closepath" is followed immediately by a "moveto", then the "moveto" identifies the start point of the next
         subpath. If a "closepath" is followed immediately by any other command, then the next subpath starts at the
         same initial point as the current subpath.
         */

        // Close the subpath with a straight geom
        Point newPos = subpathStartPoints.pollLast();
        lines.add(new StraightLine(currentPos, newPos));
        currentPos = newPos;
    }

    /* lineto */

    private void cmdLineTo(Command cmd) {
        // Iterate pairs
        for (int i = 0; i < cmd.args().size() / 2; i++) {
            Point newPos;
            if (cmd.isRelative()) {
                newPos = currentPos.plus(
                        new Point(cmd.args().get(i*2), cmd.args().get(i*2+1))
                );
            } else {
                newPos = new Point(cmd.args().get(i*2), cmd.args().get(i*2+1));
            }

            lines.add(new StraightLine(currentPos, newPos));
            currentPos = newPos;
        }
    }

    private void cmdLineToHorizontal(Command cmd) {
        double newX = cmd.isRelative() ? currentPos.getX() : 0;

        for (double d : cmd.args()) { newX += d; }

        Point newPos = new Point(newX, currentPos.getY());
        lines.add(new StraightLine(currentPos, newPos));
        currentPos = newPos;
    }

    private void cmdLineToVertical(Command cmd) {
        double newY = cmd.isRelative() ? currentPos.getY() : 0;

        for (double d : cmd.args()) { newY += d; }

        Point newPos = new Point(currentPos.getX(), newY);
        lines.add(new StraightLine(currentPos, newPos));
        currentPos = newPos;
    }

    private void cmdLineToCubicBezier(Command cmd) {
        for (int i = 0; i < cmd.args().size() / 6; i++) {
            List<Double> args = cmd.args();
            Point p1 = currentPos;
            Point p2 = new Point(args.get(i*6  ), args.get(i*6+1));
            Point p3 = new Point(args.get(i*6+2), args.get(i*6+3));
            Point p4 = new Point(args.get(i*6+4), args.get(i*6+5));

            if (cmd.isRelative()) {
                p2 = p2.plus(currentPos);
                p3 = p3.plus(currentPos);
                p4 = p4.plus(currentPos);
            }

            lines.add(new CubicBezierCurve(p1, p2, p3, p4));
            positionBeforeLastCommand = currentPos;
            currentPos = p4;
        }
    }

    private int test = 0;

    private void cmdLineToCubicBezierShorthand(Command cmd) {
        Point lastIterationP3 = null;
        for (int i = 0; i < cmd.args().size() / 4; i++) {
            List<Double> args = cmd.args();
            // Getting point 1, 3 and 4 is easy
            Point p1 = currentPos;
            Point p2;
            Point p3 = new Point(args.get(i*4  ), args.get(i*4+1));
            Point p4 = new Point(args.get(i*4+2), args.get(i*4+3));
            Color color = Color.BLACK;

            // Determine point 2. If the last command was not a cubic bézier operation we will assign it to p1
            if (i == 0) {
                if (lastCommand != null &&
                        (Character.toLowerCase(lastCommand.command) == 'c' || Character.toLowerCase(lastCommand.command) == 's')) {

                    List<Double> lastCmdArgs = lastCommand.args();
                    Point lastP3;
                    if (Character.toLowerCase(lastCommand.command) == 'c') {
                        lastP3 = new Point(
                                lastCmdArgs.get((lastCmdArgs.size()/6-1) * 6 + 2),
                                lastCmdArgs.get((lastCmdArgs.size()/6-1) * 6 + 3));
                    } else {
                        lastP3 = new Point(
                                lastCmdArgs.get((lastCmdArgs.size()/4-1) * 4),
                                lastCmdArgs.get((lastCmdArgs.size()/4-1) * 4 + 1));
                    }

                    if (lastCommand.isRelative()) {
                        lastP3 = lastP3.plus(positionBeforeLastCommand);
                    }
                    lastP3 = lastP3.minus(currentPos);
                    p2 = lastP3.multiply(-1); // Mirror it
                } else {
                    p2 = cmd.isRelative() ? new Point(0,0) : p1;
                }
            } else {
                //noinspection ConstantConditions
                p2 = lastIterationP3.multiply(-1); // Mirror it
            }

            lastIterationP3 = p3;

            if (cmd.isRelative()) {
                p2 = p2.plus(currentPos);
                p3 = p3.plus(currentPos);
                p4 = p4.plus(currentPos);
            }

            CubicBezierCurve curve = new CubicBezierCurve(p1, p2, p3, p4);
            curve.setColor(color);
            lines.add(curve);
            positionBeforeLastCommand = p4;
            currentPos = p4;
            test++;
        }
    }

    private class Command {
        final char command;
        private final String argsRaw;
        private final List<Double> argsDouble;

        private Command(char command, String args) {
            this.command = command;
            this.argsRaw = args;
            this.argsDouble = SvgUtil.parsePathCommandArgs(args);
        }

        private Command(char command, List<Double> args) {
            this.command = command;

            List<String> argsAsStrings = new LinkedList<>();
            args.forEach(d -> argsAsStrings.add(d.toString()));

            this.argsRaw = String.join(" ", argsAsStrings);
            this.argsDouble = args;
        }

        /**
         * In the SVG path standard a path is relative if the command is lowercase, or absolute otherwise
         *
         * @return true if the command is relative
         */
        boolean isRelative() {
            return Character.isLowerCase(command);
        }

        List<Double> args() {
            return argsDouble;
        }

        public String getArgsRaw() {
            return argsRaw;
        }

        @Override
        public String toString() {
            return "Command{" +
                    "command=" + command +
                    ", args=" + args() +
                    '}';
        }
    }

    public List<Line> getLines() {
        return lines;
    }
}
