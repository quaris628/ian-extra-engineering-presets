package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.iface.ArtemisNetworkInterface;
import com.walkertribe.ian.iface.ThreadedArtemisNetworkInterface;
import com.walkertribe.ian.protocol.core.eng.EngResetCoolantPacket;
import com.walkertribe.ian.protocol.core.eng.EngSetCoolantPacket;
import com.walkertribe.ian.protocol.core.eng.EngSetEnergyPacket;
import com.walkertribe.ian.protocol.core.setup.SetShipPacket;

import java.io.IOException;

public class ExtraEngPresetsClient {

    private final ExtraEngPresetsConfig config;
    private ArtemisNetworkInterface server;

    public ExtraEngPresetsClient(ExtraEngPresetsConfig config) {
        this.config = config;
        this.server = null;
    }

    public void start() throws IOException {
        server = new ThreadedArtemisNetworkInterface(
                config.getServerIpAddress(),
                config.getServerPort());
        server.start();
        server.send(new SetShipPacket(config.getShipIndex()));
    }

    public void applyPreset(String key) {
        // be mindful of keeping input delay low here
        EngSysSetting[] highPowerSystems =
                config.getPresets().getPresetHighPowerSystems(key);
        EngSysSetting[] lowPowerSystems =
                config.getPresets().getPresetLowPowerSystems(key);

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
        server.send(new EngSetEnergyPacket(
                setting.getSystem(), setting.getPower0to1()));
    }

    private void setCoolant(EngSysSetting setting) {
        if (setting.getCoolant() > 0) {
            server.send(new EngSetCoolantPacket(
                    setting.getSystem(), setting.getCoolant()));
        }
    }

    public ExtraEngPresetsConfig getConfig() {
        return config;
    }

}
