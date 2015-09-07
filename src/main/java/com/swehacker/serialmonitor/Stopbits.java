package com.swehacker.serialmonitor;

public enum Stopbits {
    ONE(1),
    TWO(2),
    ONE_HALF(3);

    private final int bits;
    private Stopbits(int bits) {
        this.bits = bits;
    }

    public int getBits() { return bits; }

    /**
     * Convert stopbits from string to int.
     */
    public static int getBits(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return ONE_HALF.getBits();
        }
    }
}
