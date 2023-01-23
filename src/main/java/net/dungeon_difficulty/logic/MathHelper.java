package net.dungeon_difficulty.logic;

public class MathHelper {
    public static double round(double value, double unit) {
        return Math.round(value / unit) * unit;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }
}
