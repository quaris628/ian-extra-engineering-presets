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
            // be mindful of keeping input delay low here
            String inputLine = sc.nextLine();
            applyPreset(config.getPresets().getPreset(inputLine));
        }
    }

    public void applyPreset(EngSysSetting[] preset) {
        // be mindful of keeping input delay low here
        server.send(new EngResetCoolantPacket());
        for (EngSysSetting setting : preset) {
            server.send(new EngSetEnergyPacket(setting.getSystem(), setting.getPower0to1()));
            if (setting.getCoolant() > 0) {
                server.send(new EngSetCoolantPacket(setting.getSystem(), setting.getCoolant()));
            }
        }
    }

}
