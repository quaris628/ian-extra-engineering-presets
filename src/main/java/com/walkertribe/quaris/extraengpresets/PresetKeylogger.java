package com.walkertribe.quaris.extraengpresets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class PresetKeylogger {

    private final ExtraEngPresetsConfig config;
    private final HashMap<String, Integer> presetUseCount;
    private final HashMap<String, Long> presetDurationActive;
    private String prevPresetKey;
    private long  prevPresetAppliedTimestamp;

    public PresetKeylogger(ExtraEngPresetsConfig config) {
        this.config = config;
        this.presetUseCount = new HashMap<String, Integer>();
        this.presetDurationActive = new HashMap<String, Long>();
        for (String presetKey : config.getPresets().getKeys()) {
            this.presetUseCount.put(presetKey, 0);
            this.presetDurationActive.put(presetKey, 0L);
        }
        this.prevPresetKey = null;
    }

    /**
     * Log that a preset was used
     * @param key preset's keybind
     */
    public void log(String key) {
        long currentTime = System.currentTimeMillis();
        if (this.prevPresetKey != null) {
            long prevPresetTimeElapsed = currentTime - this.prevPresetAppliedTimestamp;
            this.presetDurationActive.put(this.prevPresetKey,
                    this.presetDurationActive.get(this.prevPresetKey) + prevPresetTimeElapsed);
        }

        this.presetUseCount.put(key, this.presetUseCount.get(key) + 1);

        this.prevPresetKey = key;
        this.prevPresetAppliedTimestamp = currentTime;
    }

    /**
     * Write info to a file
     */
    public void writeLog() throws IOException {
        long currentTime = System.currentTimeMillis();

        // finish recording time elapsed for last preset
        long prevPresetTimeElapsed = currentTime - this.prevPresetAppliedTimestamp;
        this.presetDurationActive.put(this.prevPresetKey,
                this.presetDurationActive.get(this.prevPresetKey) + prevPresetTimeElapsed);
        // leave prevPresetKey set to what it is
        this.prevPresetAppliedTimestamp = currentTime;

        // Ensure logs folder exists
        new File("logs/").mkdir();

        // create file
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(currentTime));
        File logFile = new File("logs/preset-use-log-" + timestamp + ".csv");
        if (!logFile.createNewFile()) {
            throw new IOException("Could not create log file. Reason unknown.");
        }

        // write to file
        FileWriter fileWriter = new FileWriter(logFile.getAbsolutePath());
        fileWriter.write(HEADER_LINE);
        for (String key : config.getPresets().getKeys()) {
            fileWriter.write(formatLineOfPresetData(key));
        }
        fileWriter.close();
    }


    private String formatLineOfPresetData(String key) {
        int useCount = this.presetUseCount.get(key);
        double secondsActive = this.presetDurationActive.get(key) / 1000d;

        return formatLineOfPresetData(key, useCount, secondsActive);
    }

    private final static String HEADER_LINE = "Key,Uses,Total seconds active\n";
    private static String formatLineOfPresetData(String key, int useCount, double secondsActive) {
        return key + ","
                + useCount + ","
                + formatDouble(secondsActive) + "\n";
    }

    private static String formatDouble(double num) {
        return String.valueOf(Math.round(num * 100) / 100d);
    }

}
