package org.tbstcraft.quark.foundation.command.assertion;

public final class NumberLimitation {
    private final double min;
    private final double max;

    public NumberLimitation(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static NumberLimitation bound(double min, double max) {
        return new NumberLimitation(min, max);
    }

    public static NumberLimitation moreThan(double min) {
        return new NumberLimitation(min, Double.MAX_VALUE);
    }

    public static NumberLimitation lessThan(double max) {
        return new NumberLimitation(Double.MIN_VALUE, max);
    }

    public static NumberLimitation any() {
        return new NumberLimitation(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public void test(double number, int position) {
        if (number < this.min || number > this.max) {
            throw new ArgumentAssertionException("argument-bound", position, this.min, this.max, number);
        }
    }
}
