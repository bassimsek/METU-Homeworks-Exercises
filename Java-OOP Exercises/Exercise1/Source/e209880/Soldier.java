package e209880;
/** A class that represents general Soldier objects
 *  for our environment. It is parent of all Soldier types(Commandos, Snipers, Regular Soldiers).
 */
public abstract class Soldier extends SimulationObject {

    // constants
    private final double soldierType;
    private final double collisionRange;
    private final double shootingRange;
    // changeable
    private SoldierState soldierState;


    /** Constructor for soldier type objects.
     * Soldiers' state,type,collision range and shooting range are initialized. */
    public Soldier(String name, Position position, double speed, double soldierType, double collisionRange, double shootingRange) {
        super(name,position,speed);
        this.soldierState = SoldierState.SEARCHING;
        this.soldierType = soldierType;
        this.collisionRange = collisionRange;
        this.shootingRange = shootingRange;

    }



    /** Creates bullet and adds it to our simulation environment. */
    public void createBullet (double speed, SimulationController controller) {
        Bullet bullet = new Bullet(this.getPosition(),speed);
        bullet.setDirection(this.getDirection());
        controller.addedBullets.add(bullet);
        System.out.println(this.getName() + " fired " + bullet.getName() + " to direction " + this.getDirection() + ".");
    }


    /** Checks whether the given closest zombie is in shooting range of the soldier or not. */
    public boolean isClosestZombieInShootingRange (SimulationObject closestZombie) {
        if (this.getPosition().distance(closestZombie.getPosition()) <= this.getShootingRange()) {
            return true;
        } else {
            return false;
        }
    }


    /** Changes state of soldier to given state. */
    public void changeStateTo (SoldierState state) {
        this.setSoldierState(state);
        System.out.println(this.getName() + " changed state to " + this.getSoldierState()+".");
    }


    /** Getter method for soldierType field. */
    public SoldierType getSoldierType() {
        SoldierType type =null;
        for(int i=0;i<SoldierType.values().length;i++) {
            if (SoldierType.values()[i].getValue() == this.soldierType) {
                type = SoldierType.values()[i];
            }
        }
        return type;
    }

    /** Getter method for collisionRange field. */
    public double getCollisionRange() {
        return collisionRange;
    }

    /** Getter method for shootingRange field. */
    public double getShootingRange() {
        return shootingRange;
    }

    /** Getter method for soldierState field. */
    public SoldierState getSoldierState() {
        return soldierState;
    }

    /** Setter method for soldierState field. */
    public void setSoldierState(SoldierState soldierState) {
        this.soldierState = soldierState;
    }
}
