package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.enums.ShipSystem;
import com.walkertribe.ian.iface.*;
import com.walkertribe.ian.protocol.core.eng.EngSetCoolantPacket;
import com.walkertribe.ian.protocol.core.eng.EngSetEnergyPacket;
import com.walkertribe.ian.protocol.core.setup.SetShipPacket;
import com.walkertribe.ian.util.PlayerShipUpdateListener;
import com.walkertribe.ian.world.Artemis;
import com.walkertribe.ian.world.ArtemisPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class ExtraEngPresetsClient {

    private static final String CONFIG_FILEPATH = "config.ini";
    private static final String SERVER_ADDRESS_INI_FIELD = "serverAddress=";
    private static final String SHIP_NUM_INI_FIELD = "shipNumber=";
    private static final String PRESET_FILEPATH_INI_FIELD = "presetFile=";

    private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
    private static final byte DEFAULT_SHIP_INDEX = 0;
    private static final String DEFAULT_PRESETS_FILEPATH = "presets.txt";

    private static final String CONSOLE_QUIT_MSG = "QUIT";
	
    public static void main(String[] args) {
        // Read INI file
        Scanner iniSc;
        try {
            iniSc = new Scanner(new File(CONFIG_FILEPATH));
        } catch (FileNotFoundException e) {
            System.out.println("Could not open " + CONFIG_FILEPATH + " file for editing:");
            e.printStackTrace();
            new Scanner(System.in).nextLine();
            return;
        }

        // Parse INI file
        String serverAddress = DEFAULT_SERVER_ADDRESS;
        byte shipIndex = DEFAULT_SHIP_INDEX;
        String presetsFilePath = DEFAULT_PRESETS_FILEPATH;
        while (iniSc.hasNextLine()) {
            String line = iniSc.nextLine();
            if (line.startsWith(SERVER_ADDRESS_INI_FIELD)) {
                serverAddress = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith(SHIP_NUM_INI_FIELD)) {
                String shipNumStr = line.substring(line.indexOf("=") + 1);
                shipIndex = (byte)(Byte.parseByte(shipNumStr) - 1);
                if (!(0 <= shipIndex && shipIndex <= 7)) {
                    System.out.println("Ship number must be between 1 and 8 (inclusive). It was: " + shipNumStr);
                    new Scanner(System.in).nextLine();
                    return;
                }
            } else if (line.startsWith(PRESET_FILEPATH_INI_FIELD)) {
                presetsFilePath = line.substring(line.indexOf("=") + 1);
            }
        }

        // Check if presets filepath is valid
        File presetsFile = new File(presetsFilePath);
        if (presetsFile.isDirectory()) {
            System.out.println("presetsFilePath must point to a file, not a directory. It was: " + presetsFilePath);
            new Scanner(System.in).nextLine();
            return;
        }
        if (!presetsFile.isFile()) {
            System.out.println("This file does not exist:" + presetsFile.getAbsolutePath()
                    + "\nDo you want to create it and open it for editing? (Y/N) ");
            String response = new Scanner(System.in).next();
            if (!response.equalsIgnoreCase("Y")) {
                return;
            }
            try {
                if (!presetsFile.createNewFile()) {
                    System.out.println("ERROR: Could not create presets file. Reason unknown.");
                    System.out.println("Perhaps try creating it yourself?");
                    new Scanner(System.in).nextLine();
                    return;
                }
            } catch (IOException e) {
                System.out.println("Could not create presets file:");
                e.printStackTrace();
                new Scanner(System.in).nextLine();
                return;
            }
            try {
                new ProcessBuilder("Notepad.exe", presetsFile.getAbsolutePath()).start();
            } catch (IOException e) {
                System.out.println("Could not open presets file for editing:");
                new Scanner(System.in).nextLine();
                e.printStackTrace();
                return;
            }
            return;
        }

        // Start the client
        ExtraEngPresetsClient client;
        try {
            client = new ExtraEngPresetsClient(serverAddress, shipIndex, presetsFilePath);
        } catch (IOException ex) {
            System.out.println("Could not read from presets file:");
            ex.printStackTrace();
            new Scanner(System.in).nextLine();
            return;
        }

        // Listen to command line
        client.listenToConsoleInput();
    }

    private ArtemisNetworkInterface server;
    private ExtraPresetsReader presetsReader;
	
    public ExtraEngPresetsClient(String host, byte shipIndex, String presetsFilePath) throws IOException {
        System.out.println("Reading preset documents...");
        presetsReader = new ExtraPresetsReader(presetsFilePath);
        System.out.println("Preset documents successfully obtained from " + presetsFilePath);

        System.out.println("Connecting to " + host + "...");
        int port = Artemis.DEFAULT_PORT;
        int colonPos = host.indexOf(':');

        if (colonPos != -1) {
            port = Integer.parseInt(host.substring(colonPos + 1));
            host = host.substring(0, colonPos);
        }

        server = new ThreadedArtemisNetworkInterface(host, port);
        server.addListener(this);
        server.start();
        System.out.println("Connected!");
        server.send(new SetShipPacket(shipIndex));
        System.out.println("Our agents have boarded ship " + (shipIndex + 1)
                + " undetected and are awaiting your command.");
    }

    public void listenToConsoleInput() {
        Scanner sc = new Scanner(System.in);
        String inputLine = sc.nextLine();
        while (!inputLine.equals(CONSOLE_QUIT_MSG)) {
            EngSysSetting[] preset = presetsReader.getPreset(inputLine);
            if (preset != null) {
                // send it twice in case packets are lost
                applyPreset(preset);
                applyPreset(preset);
            } else {
                System.out.println("There's no preset with key: " + inputLine);
            }
            inputLine = sc.nextLine();
        }
    }

    public void applyPreset(EngSysSetting[] preset) {
        if (preset.length != 8) {
            throw new IllegalArgumentException("preset must be for all 8 systems. Length was " + preset.length);
        }
        for (int i = 0; i < ShipSystem.values().length; i++) {
            ShipSystem sys = ShipSystem.values()[i];
            EngSysSetting setting = preset[i];
            server.send(new EngSetEnergyPacket(sys, setting.getPowerDec()));
            server.send(new EngSetCoolantPacket(sys, setting.getCoolant()));
        }
    }
}
