package net.powerscale.logic;

public class MathHelper {
    public static double round(double value, double unit) {
        return Math.round(value / unit) * unit;
    }
}
