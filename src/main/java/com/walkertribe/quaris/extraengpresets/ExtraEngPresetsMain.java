package com.walkertribe.quaris.extraengpresets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Scanner;

public class ExtraEngPresetsMain {

    private final static String LOG_COMMAND = "LOG";


    public static void main(String[] args) {
        ExtraEngPresetsConfig config = loadConfigFile();
        if (config == null) {
            return;
        }
        PresetUseLogger logger = new PresetUseLogger(config);

        ExtraEngPresetsClient client = new ExtraEngPresetsClient(config);
        if (!startClient(client)) {
            return;
        }
        listenToConsoleInput(client, logger);
    }

    private static ExtraEngPresetsConfig loadConfigFile() {
        ExtraEngPresetsConfig config = null;
        try {
            config = ExtraEngPresetsConfig.FromFile();
            System.out.println("Obtained "
                    + config.getPresets().getNumberOfPresets()
                    + " preset documents from "
                    + config.getPresetsFilePath());
        } catch (FileNotReadyException e) {

        } catch (FileNotFoundException e) {
            System.out.println("File not found error");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
        } catch (NumberFormatException e) {
            System.out.println("Number format error");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
        } catch (IOException e) {
            System.out.println("Error reading file");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
        }
        return config;
    }

    private static boolean startClient(ExtraEngPresetsClient client) {
        // Start the artemis client
        boolean connectionSucceeded = false;
        while (!connectionSucceeded) {
            try {
                System.out.println("Connecting with access code "
                        + client.getConfig().getFullServerAddress());
                client.start();
                connectionSucceeded = true;
            } catch (ConnectException e) {
                System.out.println("Connection failed. Retrying.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                new Scanner(System.in).nextLine();
                return false;
            }
        }
        System.out.println("Access codes accepted. "
                + "Our agents have boarded ship "
                + (client.getConfig().getShipNumber())
                + " undetected and are awaiting your command.");
        return true;
    }

    private static void listenToConsoleInput(ExtraEngPresetsClient client,
                                             PresetUseLogger logger) {
        Scanner sc = new Scanner(System.in);
        String inputLine = "";
        while (true) {
            try {
                while (true) {
                    // be mindful of keeping input delay low here
                    inputLine = sc.nextLine();
                    client.applyPreset(inputLine);
                    logger.log(inputLine);
                }
            } catch (Exception e) {
                if (inputLine.equalsIgnoreCase(LOG_COMMAND)) {
                    try {
                        logger.writeLog();
                        System.out.println("Log successful");
                    } catch (IOException ex) {
                        System.out.println("Log failed");
                        ex.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        }
    }


}
