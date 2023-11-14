package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.enums.ShipSystem;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Stores a map of keybinds to engineering presets.
 */
public class ExtraPresets
{
    private static final char COMMENT_CHAR = ';';
    private static final char POWER_COOLANT_DELIMITER = '\t';

    private HashMap<String, EngSysSetting[]> presets;

    /**
     * Creates extra presets object by loading keybinds and settings from a file
     * @param file presets file to load
     * @throws FileNotFoundException if the file isn't found
     * @throws IOException if there's problems parsing the power and coolant
     */
    public ExtraPresets(File file) throws IOException, FileNotFoundException {
        presets = new HashMap<String, EngSysSetting[]>();

        Scanner sc = new Scanner(file);
        String line;
        // loop through each preset
        while (sc.hasNextLine()) {
            line = trimWhitespace(trimComment(sc.nextLine()));
            if (line.length() == 0) {
                continue;
            }
            // read preset's key
            String key = line;
            // read preset's system settings
            EngSysSetting[] settings = new EngSysSetting[ShipSystem.values().length];
            for (ShipSystem sys : ShipSystem.values()) {
                int[] powerAndCoolant = parsePowerAndCoolant(sc.nextLine());
                settings[sys.ordinal()] = new EngSysSetting(sys, powerAndCoolant[0], powerAndCoolant[1]);
            }
            // sort systems settings by power level, so that the
            // higher-power packets are the ones that are sent first
            Arrays.sort(settings, Comparator.comparingInt(EngSysSetting::getPower0to300));

            presets.put(key, settings);
        }
    }

    /**
     * Gets a preset for a given key.
     * Returned preset array indices are the ordinals of the ShipSystem enum.
     * @param key a unique string that identifies this preset
     *   (this is currently always the preset's autohotkey keybind, but it doesn't have to be)
     * @return presets for the key, or DEFAULT_PRESET if the key doesn't have a preset
     */
    public EngSysSetting[] getPreset(String key) {
        // be mindful of keeping input delay low here
        return presets.get(key);
    }

    /**
     * Parses power and coolant from a line in the presets file
     * @param line single line in the presets file
     * @return int[] { power, coolant }
     * @throws IOException if there's problems parsing the integers
     */
    private static int[] parsePowerAndCoolant(String line) throws IOException {
        line = trimComment(line);
        if (trimWhitespace(line).length() == 0) {
            throw new IOException("Blank power and coolant line");
        }
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
