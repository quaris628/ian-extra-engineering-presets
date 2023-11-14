package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.enums.ShipSystem;

import java.io.*;
import java.util.*;

/**
 * Stores a map of keybinds to engineering presets.
 */
public class ExtraPresets
{
    private static final char COMMENT_CHAR = ';';
    private static final char POWER_COOLANT_DELIMITER = '\t';

    private HashMap<String, EngSysSetting[]> presets;
    private HashMap<String, EngSysSetting[]> presetsHighPowerSystems;
    private HashMap<String, EngSysSetting[]> presetsLowPowerSystems;

    /**
     * Creates extra presets object by loading keybinds and settings from a file
     * @param file presets file to load
     * @throws FileNotFoundException if the file isn't found
     * @throws IOException if there's problems parsing the power and coolant
     */
    public ExtraPresets(File file) throws IOException, FileNotFoundException {
        presets = new HashMap<String, EngSysSetting[]>();
        presetsHighPowerSystems = new HashMap<String, EngSysSetting[]>();
        presetsLowPowerSystems = new HashMap<String, EngSysSetting[]>();

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
            LinkedList<EngSysSetting> highPowerSettings = new LinkedList<EngSysSetting>();
            LinkedList<EngSysSetting> lowPowerSettings = new LinkedList<EngSysSetting>();
            for (ShipSystem sys : ShipSystem.values()) {
                int[] powerAndCoolant = parsePowerAndCoolant(sc.nextLine());
                int power = powerAndCoolant[0];
                int coolant = powerAndCoolant[1];
                EngSysSetting setting = new EngSysSetting(sys, power, coolant);
                settings[sys.ordinal()] = setting;
                if (power > 100) {
                    highPowerSettings.add(setting);
                } else {
                    lowPowerSettings.add(setting);
                }
            }
            // sort systems settings by power level
            Arrays.sort(settings, Comparator.comparingInt(EngSysSetting::getPower0to300));

            presets.put(key, settings);
            presetsHighPowerSystems.put(key, manualToArray(highPowerSettings));
            presetsLowPowerSystems.put(key, manualToArray(lowPowerSettings));
        }
    }

    /**
     * Gets a preset for a given key.
     * @param key a unique string that identifies this preset
     *   (this is currently always the preset's autohotkey keybind, but it doesn't have to be)
     * @return preset for the key
     */
    public EngSysSetting[] getPreset(String key) {
        // be mindful of keeping input delay low here
        return presets.get(key);
    }

    /**
     * Gets the preset's systems whose power is over 100% for a given key.
     * @param key a unique string that identifies this preset
     *   (this is currently always the preset's autohotkey keybind, but it doesn't have to be)
     * @return system settings for high-power systems of the preset for the key
     */
    public EngSysSetting[] getPresetHighPowerSystems(String key) {
        // be mindful of keeping input delay low here
        return presetsHighPowerSystems.get(key);
    }

    /**
     * Gets the preset's systems whose power is at or under 100% for a given key.
     * @param key a unique string that identifies this preset
     *   (this is currently always the preset's autohotkey keybind, but it doesn't have to be)
     * @return system settings for low-power systems of the preset for the key
     */
    public EngSysSetting[] getPresetLowPowerSystems(String key) {
        // be mindful of keeping input delay low here
        return presetsLowPowerSystems.get(key);
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

    private static EngSysSetting[] manualToArray(Collection<EngSysSetting> settings) {
        // toArray returns Object[] and when casting to EngSysSetting[] I get this runtime error:
        // "class [Ljava.lang.Object; cannot be cast to class
        //  [Lcom.walkertribe.quaris.extraengpresets.EngSysSetting;
        //  ([Ljava.lang.Object; is in module java.base of loader 'bootstrap';
        //  [Lcom.walkertribe.quaris.extraengpresets.EngSysSetting;
        //  is in unnamed module of loader 'app')
        // so this manualToArray is a workaround
        EngSysSetting[] settingsArr = new EngSysSetting[settings.size()];
        int i = 0;
        for (EngSysSetting setting: settings) {
            settingsArr[i++] = setting;
        }
        return settingsArr;
    }
}
