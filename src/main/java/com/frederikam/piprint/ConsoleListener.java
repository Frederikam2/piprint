package com.frederikam.piprint;

import com.frederikam.piprint.print.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class ConsoleListener extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ConsoleListener.class);

    private final Workspace workspace;

    public ConsoleListener(Workspace workspace) {
        this.workspace = workspace;
        setDaemon(true);
        setName("ConsoleListener");
        String str = "Available commands:" +
                        "\ns:    Stop" +
                        "\nr:    Resume" +
                        "\ne:    Emergency stop";
        log.info(str);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        //noinspection InfiniteLoopStatement
        while (true) {
            String command = scanner.nextLine();

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
