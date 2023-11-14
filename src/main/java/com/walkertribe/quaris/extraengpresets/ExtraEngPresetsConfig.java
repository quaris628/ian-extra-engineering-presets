package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.world.Artemis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ExtraEngPresetsConfig {

    private static final String CONFIG_FILEPATH = "config.ini";

    private static final String SERVER_ADDRESS_INI_FIELD = "serverAddress=";
    private static final String SHIP_NUM_INI_FIELD = "shipNumber=";
    private static final String PRESET_FILEPATH_INI_FIELD = "presetFile=";

    private static final String DEFAULT_SERVER_IP_ADDRESS = "127.0.0.1";
    private static final byte DEFAULT_SHIP_INDEX = 0;
    private static final String DEFAULT_PRESETS_FILEPATH = "presets.txt";

    private String serverIpAddress;
    private int serverPort;
    private byte shipIndex;
    private ExtraPresets presets;
    // isEasternFront is only relevant for autohotkey

    /**
     * Creates a config object by reading from the CONFIG_FILEPATH file.
     * @throws FileNotFoundException when file wasn't initially found
     *    (even if it was successfully created and opened for editing)
     * @throws IOException when file format is invalid, or when file's
     *    auto-creation and/or open-for-editing was attempted and fails
     * @throws NumberFormatException when file's number format is invalid
     */
    public ExtraEngPresetsConfig()
            throws IOException, FileNotFoundException, IllegalArgumentException {
        Scanner iniSc = new Scanner(validateFilepath(CONFIG_FILEPATH, false));

        this.serverIpAddress = DEFAULT_SERVER_IP_ADDRESS;
        this.serverPort = Artemis.DEFAULT_PORT;
        this.shipIndex = DEFAULT_SHIP_INDEX;
        String presetsFilePath = DEFAULT_PRESETS_FILEPATH;

        while (iniSc.hasNextLine()) {
            String line = iniSc.nextLine();

            if (line.startsWith(SERVER_ADDRESS_INI_FIELD)) {
                String fullServerAddressStr = line.substring(line.indexOf("=") + 1);
                this.serverIpAddress = parseServerIpAddress(fullServerAddressStr);
                this.serverPort = parseServerPort(fullServerAddressStr);
            } else if (line.startsWith(SHIP_NUM_INI_FIELD)) {
                String shipNumStr = line.substring(line.indexOf("=") + 1);
                this.shipIndex = parseShipIndex(shipNumStr);
            } else if (line.startsWith(PRESET_FILEPATH_INI_FIELD)) {
                presetsFilePath = line.substring(line.indexOf("=") + 1);
            }
        }

        this.presets = new ExtraPresets(validateFilepath(presetsFilePath, true));
    }

    /**
     * Validates a filepath is valid to read from.
     * If the file isn't found, asks the user if they want the file created for them
     *   and opened for editing (and then does that if the user answered yes).
     * @param filepath of the file to open
     * @param isPresetFile true if validating preset file; false if validating config.ini
     * @return Scanner object reading from the file
     * @throws FileNotFoundException when file wasn't initially found
     *   (even if it was successfully created and opened for editing)
     * @throws IOException when file's auto-creation and/or open-for-editing
     *   was attempted and fails
     */
    private static File validateFilepath(String filepath, boolean isPresetFile) throws IOException, FileNotFoundException {
        File file = new File(filepath);

        if (file.isDirectory()) {
            throw new FileNotFoundException(
                    (isPresetFile ? "presetFile" : "CONFIG_FILEPATH")
                    + " must point to a file, not a directory. It was: "
                            + filepath);
        }

        if (!file.isFile()) {
            if (isPresetFile) {
                System.out.println("This file does not exist: " + file.getAbsolutePath()
                        + "\nDo you want to create it and open it for editing? (Y/N) ");
                if (new Scanner(System.in).next().equalsIgnoreCase("Y")) {
                    if (!file.createNewFile()) {
                        throw new IOException("Could not create presets file. Reason unknown.");
                    }
                    FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
                    fileWriter.write(generateNewPresetsTxt());
                    fileWriter.close();
                    System.out.println("Generated file " + file.getAbsolutePath());
                    new ProcessBuilder("Notepad.exe", file.getAbsolutePath()).start();
                    System.out.println("Opened file for editing in Notepad");
                }
            }
            throw new FileNotFoundException("This file does not exist: " + file.getAbsolutePath());
        }

        return file;
    }

    private static int parseServerPort(String fullServerAddressStr) throws NumberFormatException {
        int colonPos = fullServerAddressStr.indexOf(':');
        if (colonPos > 0) {
            return Integer.parseInt(fullServerAddressStr.substring(colonPos + 1));
        } else {
            return Artemis.DEFAULT_PORT;
        }
    }

    private static String parseServerIpAddress(String fullServerAddressStr) {
        int colonPos = fullServerAddressStr.indexOf(':');
        if (colonPos > 0) {
            return fullServerAddressStr.substring(0, colonPos);
        } else {
            return fullServerAddressStr;
        }
    }

    private static byte parseShipIndex(String shipNumStr) throws NumberFormatException {
        byte shipIndex = (byte)(Integer.parseInt(shipNumStr) - 1);
        if (!(0 <= shipIndex && shipIndex <= 7)) {
            throw new NumberFormatException(
                    "Ship number must be between 1 and 8 (inclusive). It was: "
                            + shipNumStr);
        }
        return shipIndex;
    }

    public String getFullServerAddress() {
        return serverIpAddress + ":" + serverPort;
    }
    public String getServerIpAddress() {
        return serverIpAddress;
    }
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Index from 0 to 7 indicating an artemis ship
     */
    public byte getShipIndex() {
        return shipIndex;
    }

    /**
     * Number from 1 to 8 indicating an artemis ship
     */
    public byte getShipNumber() {
        return (byte) (shipIndex + 1);
    }
    public ExtraPresets getPresets() {
        // be mindful of keeping input delay low here
        return presets;
    }

    private static String generateNewPresetsTxt() {
        return "; Notes:\n" +
                ";\n" +
                "; Be mindful of overlaps with your other engineering keybinds.\n" +
                "; ESPECIALLY be mindful of your presets for storing keybinds.\n" +
                "; It's an easy mistake to add extra presets for Shift+1, etc.\n" +
                "; and not change your keybinds for storing presets.\n" +
                "; (To change your Artemis keybinds, edit controls.ini.)\n" +
                ";\n" +
                "; These extra presets have a slightly longer input delay\n" +
                "; compared to the 10 true presets. I haven't quantified the\n" +
                "; difference, but I recommend using the true presets for\n" +
                "; panic and/or combat settings that need to be fast.\n" +
                "\n" +
                "\n" +
                "; File Format:\n" +
                "; \n" +
                "; Each preset must be 9 consecutive lines.\n" +
                "; \n" +
                "; Line 1 = the keybind. e.g. \"1\" or \"Q\"\n" +
                ";   Each keybind is assumed to be unique.\n" +
                ";   You can specify Shift and Ctrl modifiers by adding \"^\"\n" +
                ";     and \"+\" prefixes respectively. e.g. \"+Q\" is Shift+Q\n" +
                ";   More advanced keybinds are possible; any autohotkey (v2)\n" +
                ";     keybind will work. Full documentation:\n" +
                ";     https://www.autohotkey.com/docs/v2/Hotkeys.htm\n" +
                ";\n" +
                "; Lines 2-9 = the power, followed by the coolant\n" +
                ";   Power must be an integer between 0 and 300 (inclusive).\n" +
                ";   Coolant must be an integer between 0 and 8 (inclusive).\n" +
                ";   Power and coolant should be separated by one tab character.\n" +
                "\n" +
                "\n" +
                "; Sensor boost\n" +
                "E\n" +
                "0\t0 ; beams\n" +
                "75\t0 ; torpedos\n" +
                "248\t8 ; sensors\n" +
                "100\t0 ; maneuver\n" +
                "100\t0 ; impulse\n" +
                "100\t0 ; warp\n" +
                "11\t0 ; front shields\n" +
                "11\t0 ; rear shields\n" +
                "\n" +
                "; Warp boost\n" +
                "R\n" +
                "75\t0\n" +
                "75\t0\n" +
                "100\t0\n" +
                "100\t0\n" +
                "75\t0\n" +
                "248\t8\n" +
                "11\t0\n" +
                "11\t0\n";
    }
}
