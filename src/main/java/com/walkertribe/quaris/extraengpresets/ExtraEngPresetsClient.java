package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.iface.ThreadedArtemisNetworkInterface;
import com.walkertribe.ian.protocol.core.eng.EngResetCoolantPacket;
import com.walkertribe.ian.protocol.core.eng.EngSetCoolantPacket;
import com.walkertribe.ian.protocol.core.eng.EngSetEnergyPacket;
import com.walkertribe.ian.protocol.core.setup.SetShipPacket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Scanner;

public class ExtraEngPresetsClient {

    public static void main(String[] args) {
        // read config and presets files
        ExtraEngPresetsConfig config;
        try {
            config = new ExtraEngPresetsConfig();
        } catch (FileNotReadyException e) {
            return;
        } catch (FileNotFoundException e) {
            System.out.println("File not found error");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
            return;
        } catch (NumberFormatException e) {
            System.out.println("Number format error");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
            return;
        } catch (IOException e) {
            System.out.println("Error reading file");
            System.out.println(e.getMessage());
            new Scanner(System.in).nextLine();
            return;
        }

        // Start the client
        ExtraEngPresetsClient client = null;
        boolean connectionSucceeded = true;
        while (connectionSucceeded) {
            try {
                client = new ExtraEngPresetsClient(config);
                connectionSucceeded = false;
            } catch (ConnectException e) {
                System.out.println("Connection failed, retrying.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                new Scanner(System.in).nextLine();
                return;
            }
        }

        // Listen to command line
        client.listenToConsoleInput();
    }

    private ArtemisNetworkInterface server;
    private ExtraEngPresetsConfig config;

    public ExtraEngPresetsClient(ExtraEngPresetsConfig config) throws IOException {
        this.config = config;
        //System.out.println("Preset documents successfully obtained from " + presetsFilePath);

        System.out.println("Connecting to " + config.getFullServerAddress() + "...");

        server = new ThreadedArtemisNetworkInterface(config.getServerIpAddress(),
                config.getServerPort());
        server.addListener(this);
        server.start();
        System.out.println("Connected!");
        server.send(new SetShipPacket(config.getShipIndex()));
        System.out.println("Our agents have boarded ship " + (config.getShipNumber())
                + " undetected and are awaiting your command.");
    }

    public void listenToConsoleInput() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                while (true) {
                    // be mindful of keeping input delay low here
                    String inputLine = sc.nextLine();
                    applyPreset(inputLine);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void applyPreset(String key) {
        // be mindful of keeping input delay low here
        EngSysSetting[] highPowerSystems = config.getPresets().getPresetHighPowerSystems(key);
        EngSysSetting[] lowPowerSystems = config.getPresets().getPresetLowPowerSystems(key);

        // send high-power packets first
        for (EngSysSetting setting : highPowerSystems) {
            setPower(setting);
        }
        // send high-power systems' coolant packets second
        server.send(new EngResetCoolantPacket());
        for (EngSysSetting setting : highPowerSystems) {
            setCoolant(setting);
        }
        // send all other power and coolant packets last
        for (EngSysSetting setting : lowPowerSystems) {
            setPower(setting);
        }
        for (EngSysSetting setting : lowPowerSystems) {
            setCoolant(setting);
        }
    }

    private void setPower(EngSysSetting setting) {
        server.send(new EngSetEnergyPacket(setting.getSystem(), setting.getPower0to1()));
    }

    private void setCoolant(EngSysSetting setting) {
        if (setting.getCoolant() > 0) {
            server.send(new EngSetCoolantPacket(setting.getSystem(), setting.getCoolant()));
        }
    }

}
