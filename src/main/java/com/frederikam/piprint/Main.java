package com.frederikam.piprint;

public class Main {

    public static void main(String[] args) {
        String arg1 = "gui";
        if (args.length > 0)
            arg1 = args[0];

        switch (arg1) {
            case "gui":
                //TODO
                break;
            case "steptest":
                new StepTest().run();
                break;
        }
    }

}
