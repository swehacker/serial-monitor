package com.swehacker.serialmonitor;

public enum Databits {
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8);

    private final int bits;
    private Databits(int bits) {
        this.bits = bits;
    }

    public int getBits() { return bits; }

    public static int getBits(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        return 8;
    }
}
