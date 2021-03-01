package e209880;
/** Enumeration class that represents zombies' types with three instances.
 * They can be SLOW, REGULAR or FAST. */
public enum ZombieType {
    SLOW(1.0),
    REGULAR(2.0),
    FAST(3.0);

    private double val;

    /** Constructor for ZombieType instances.
     * It gets double parameter as a value for that type and initializes type with that value. */
    ZombieType (double val) {
        this.val = val;
    }

    /** Getter method for value field of ZombieType instance. */
    public double getValue() {
        return val;
    }
}