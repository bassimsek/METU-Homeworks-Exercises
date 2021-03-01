package e209880;

public enum FeatureType {
    HIGH_SPEED_TURN(1.55),
    LOW_SPEED_TURN(1.3),
    STRAIGHT(1.0);

    private double multiplier;

    FeatureType(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
