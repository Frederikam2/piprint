package com.frederikam.piprint.svg;

import java.util.LinkedList;
import java.util.List;

class SvgUtil {

    static List<Double> parsePathCommandArgs(String str) {
        LinkedList<Double> list = new LinkedList<>();
        StringBuilder currentChars = new StringBuilder();

        for (char ch : str.toCharArray()) {
            if (ch == ' ') {
                // This is a separator
                if (currentChars.length() != 0) {
                    list.add(Double.parseDouble(currentChars.toString()));
                    currentChars = new StringBuilder();
                }
            } else if (ch == '-') {
                // This either means a negative number or a new number (which is negative)
                if (currentChars.length() != 0) {
                    list.add(Double.parseDouble(currentChars.toString()));
                    currentChars = new StringBuilder();
                }
                currentChars.append(ch);
            } else if (ch == '.') {
                // This can mean that we are are already parsing the next number if we already have a decimal point
                if (currentChars.toString().contains(".")) {
                    list.add(Double.parseDouble(currentChars.toString()));
                    currentChars = new StringBuilder();
                }
                currentChars.append(ch);
            } else {
                // Digits and decimal points
                currentChars.append(ch);
            }
        }

        // Handle the last number if we don't end with a space
        if (currentChars.length() != 0) {
            list.add(Double.parseDouble(currentChars.toString()));
        }

        return list;
    }

}
