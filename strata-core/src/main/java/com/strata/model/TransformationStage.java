package com.strata.model;

/**
 * Nine-step transformation cycle.
 */
public enum TransformationStage {
    CALCINATION,
    DISSOLUTION,
    SEPARATION,
    CONJUNCTION,
    FERMENTATION,
    DISTILLATION,
    COAGULATION,
    ILLUMINATION,
    PHILOSOPHERS_STONE;

    public static final TransformationStage[] VALUES = values();

    public static byte next(byte currentStage) {
        return (byte) ((currentStage + 1) % VALUES.length);
    }

    public static String displayName(byte stage) {
        if (stage < 0 || stage >= VALUES.length) {
            return "UNKNOWN";
        }
        return VALUES[stage].name();
    }
}
