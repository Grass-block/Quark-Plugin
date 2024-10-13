package org.tbstcraft.quark;

import org.tbstcraft.quark.internal.ProductService;

public enum FeatureAvailability {
    DEMO_ONLY("demo_only"),
    DEMO_AVAILABLE("demo_available"),
    BOTH("demo_available"),
    PREMIUM("premium"),
    INHERIT("inherit");

    final String id;

    FeatureAvailability(String id) {
        this.id = id;
    }

    public static FeatureAvailability fromId(String id) {
        return switch (id) {
            case "demo_only" -> DEMO_ONLY;
            case "demo_available" -> DEMO_AVAILABLE;
            case "premium" -> PREMIUM;
            case "inherit" -> INHERIT;
            default -> throw new IllegalStateException("Unexpected value: " + id);
        };
    }

    public boolean load() {
        return switch (this) {
            case DEMO_AVAILABLE, BOTH -> true;
            case PREMIUM -> ProductService.isActivated();
            case DEMO_ONLY -> !ProductService.isActivated();
            case INHERIT -> throw new IllegalArgumentException("wtf?");
        };
    }
}
