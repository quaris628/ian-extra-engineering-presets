package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.enums.ShipSystem;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class ExtraPresetsReader
{
    private static final char COMMENT_CHAR = ';';
    private static final char POWER_COOLANT_DELIMITER = '\t';

    private HashMap<String, EngSysSetting[]> presets;

    public ExtraPresetsReader(String filepath) throws IOException {
        presets = new HashMap<String, EngSysSetting[]>();

        Scanner sc = new Scanner(new File(filepath));
        String line;
        // loop through each preset
        while (sc.hasNextLine()) {
            line = trimWhitespace(trimComment(sc.nextLine()));
            if (line.length() == 0) {
                continue;
            }
            String key = line;
            EngSysSetting[] settings = new EngSysSetting[ShipSystem.values().length];
            // loop through each system in the preset
            for (int i = 0; i < ShipSystem.values().length; i++) {
                int[] powerAndCoolant = parsePowerAndCoolant(sc.nextLine());
                settings[i] = new EngSysSetting(powerAndCoolant[0], powerAndCoolant[1]);
            }
            presets.put(key, settings);
        }
    }

    /**
     * Gets a preset for a given key
     * @param key a unique string that identifies this preset
     *            (usually a single character corresponding with the preset's keybind)
     * @return presets for the key, or null if the key doesn't have a preset
     */
    public EngSysSetting[] getPreset(String key) {
        return presets.getOrDefault(key, null);
    }

    /**
     * Parses power and coolant from a line in the presets file
     * @param line
     * @return int[] { power, coolant }
     * @throws IOException
     */
    private static int[] parsePowerAndCoolant(String line) throws IOException {
        line = trimComment(line);
        int delimPos = line.indexOf(POWER_COOLANT_DELIMITER);
        if (delimPos <= 0) {
            throw new IOException("Expected delimiter '"
                    + POWER_COOLANT_DELIMITER
                    + "' between power and coolant here: "
                    + line);
        }
        int power = Integer.parseInt(line.substring(0, delimPos).trim());
        int coolant = Integer.parseInt(line.substring(delimPos + 1).trim());
        return new int[] { power, coolant };
    }

    private static String trimComment(String line) {
        int commentPos = line.indexOf(COMMENT_CHAR);
        if (commentPos >= 0) {
            return line.substring(0, line.indexOf(COMMENT_CHAR));
        } else {
            return line;
        }
    }

    private static String trimWhitespace(String line) {
        return line.trim();
        //return line.replaceAll("\\s","");
    }
}
