package com.walkertribe.quaris.extraengpresets;

public class EngSysSetting {
    private float powerDec;
    private int coolant;

    public EngSysSetting(int power, int coolant) {
        if (!(0 <= power && power <= 300)) {
            throw new IllegalArgumentException("'power' must be between 0 and 300 (inclusive). Was " + power);
        }
        if (!(0 <= coolant && coolant <= 8)) {
            throw new IllegalArgumentException("'coolant' must be between 0 and 8 (inclusive). Was " + coolant);
        }
        this.powerDec = power * 0.00333333333333333333333f; // divide by 300
        this.coolant = coolant;
    }

    public float getPowerDec() {
        return powerDec;
    }

    public int getCoolant() {
        return coolant;
    }

    @Override
    public String toString() {
        return (powerDec * 300f) + "% power with " + coolant + " coolant";
    }
}
