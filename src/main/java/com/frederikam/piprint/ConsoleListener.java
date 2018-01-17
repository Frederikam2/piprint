package com.frederikam.piprint;

import com.frederikam.piprint.print.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Console;

public class ConsoleListener extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ConsoleListener.class);

    private final Console console;
    private final Workspace workspace;

    public ConsoleListener(Console console, Workspace workspace) {
        this.console = console;
        this.workspace = workspace;
        setDaemon(true);
        setName("ConsoleListener");
        String str = "Available commands:" +
                        "s:    Stop" +
                        "r:    Resume" +
                        "e:    Emergency stop";
        log.info(str);
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            String command = console.readLine();

            switch (command) {
                case "s":
                    if (workspace.isPaused()) {
                        log.info("The printer is already paused.");
                        break;
                    }
                    workspace.setPause(true);
                    log.info("Paused the printer");
                    break;
                case "r":
                    if (!workspace.isPaused()) {
                        log.info("The printer is already running.");
                        break;
                    }
                    log.info("Resumed the printer.");
                    workspace.setPause(false);
                    break;
                case "e":
                    log.info("Shutting down");
                    Main.gpio.shutdown();
                    System.exit(0);
            }
        }
    }
}
