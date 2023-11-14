package com.walkertribe.quaris.extraengpresets;

import com.walkertribe.ian.enums.ShipSystem;

/**
 * Settings for one engineering system (power and coolant)
 */
public class EngSysSetting {

    private ShipSystem system;
    private int power0to300;
    private float power0to1;
    private int coolant;

    public EngSysSetting(ShipSystem system, int power, int coolant) {
        if (system == null) {
            throw new IllegalArgumentException("'system' cannot be null");
        }
        if (!(0 <= power && power <= 300)) {
            throw new IllegalArgumentException("'power' must be between 0 and 300 (inclusive). Was " + power);
        }
        if (!(0 <= coolant && coolant <= 8)) {
            throw new IllegalArgumentException("'coolant' must be between 0 and 8 (inclusive). Was " + coolant);
        }
        this.system = system;
        this.power0to300 = power;
        this.power0to1 = power * 0.00333333333333333333333f; // divide by 300
        this.coolant = coolant;
    }

    public ShipSystem getSystem() {
        return system;
    }

    public int getPower0to300() {
        return power0to300;
    }
    public float getPower0to1() {
        // be mindful of keeping input delay low here
        return power0to1;
    }

    public int getCoolant() {
        // be mindful of keeping input delay low here
        return coolant;
    }

    @Override
    public String toString() {
        return system.toString() + " at " + power0to300
                + "% power with " + coolant + " coolant";
    }
}
