package com.swehacker.serialmonitor;

public enum Stopbits {
    ONE(1),
    TWO(2),
    ONE_HALF(1.5f);

    private final float bits;
    private Stopbits(float bits) {
        this.bits = bits;
    }

    public float getBits() { return bits; }
}
