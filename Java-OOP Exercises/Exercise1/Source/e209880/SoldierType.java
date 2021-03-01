package e209880;
/** Enumeration class that represents soldiers' types with three instances.
 * These instances are REGULAR, COMMANDO and SNIPER. */
public enum SoldierType {
    REGULAR(1.0),
    COMMANDO(2.0),
    SNIPER(3.0);

    private double val;

    /** Constructor for SoldierType instances.
     * It gets double parameter as a value for that type and initializes type with that value. */
    SoldierType (double val) {
        this.val = val;
    }

    /** Getter method for value field of SoldierType instance. */
    public double getValue() {
        return val;
    }
}
