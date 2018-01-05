package com.frederikam.piprint.svg;

import com.frederikam.piprint.svg.geom.Line;
import com.frederikam.piprint.svg.geom.Point;
import com.frederikam.piprint.svg.geom.StraightLine;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
    private Point currentPos = new Point(0, 0);
    private List<Line> lines = new LinkedList<>();


    /**
     * Used by the z command
     */
    private LinkedList<Point> subpathStartPoints = new LinkedList<>();

    Path(Node node) throws ParserConfigurationException, IOException, SAXException {
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
                default:
                    System.out.println("Unknown command: " + cmd.toString());
            }
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
                        new Point(cmd.args().get(0), cmd.args().get(1))
                );
            } else {
                newPos = new Point(cmd.args().get(0), cmd.args().get(1));
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
