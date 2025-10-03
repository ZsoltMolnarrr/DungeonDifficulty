package net.dungeon_difficulty.logic;

public class MathHelper {
    public static double round(double value, double unit) {
        // return Math.round( value / unit ) * unit;
        double scale = 1.0 / unit;
        return Math.round(value * scale) / scale;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }
}
