package e209880;
/** A class that represents general Zombie objects
 *  for our environment. It is parent of all Zombie types(Slow Zombies, Fast Zombies, Regular Zombies).
 */
public abstract class Zombie extends SimulationObject {

    // constants
    private final double zombieType;
    private final double collisionRange;
    private final double detectionRange;
    // changeable
    private ZombieState zombieState;



    /** Constructor for zombie type objects.
     * Zombies' state,type,collision range and detection range are initialized. */
    public Zombie(String name, Position position, double speed, double zombieType, double collisionRange, double detectionRange) {
        super(name,position,speed);
        this.zombieState = ZombieState.WANDERING;
        this.zombieType = zombieType;
        this.collisionRange = collisionRange;
        this.detectionRange = detectionRange;

    }


    /** Simulates initial behavior for all zombies for any step. */
    public boolean stepInit(SimulationController controller) {
        SimulationObject closestSoldier = findClosestEntity(controller.soldiers);
        firstStep();
        if (this.getPosition().distance(closestSoldier.getPosition()) <= this.getCollisionRange() + ((Soldier) closestSoldier).getCollisionRange()) {
            controller.removedEntities.add(closestSoldier);
            System.out.println(this.getName() + " killed " + closestSoldier.getName()+".");
            return true;
        }
        return false;

    }

    /** Checks whether the given closest soldier is in detection range of the zombie or not. */
    public boolean isClosestSoldierInDetectionRange (SimulationObject closestSoldier) {
        if (this.getPosition().distance(closestSoldier.getPosition()) <= this.getDetectionRange()) {
            return true;
        } else {
            return false;
        }
    }

    /** Changes state of zombie to given state. */
    public void changeStateTo (ZombieState state) {
        this.setZombieState(state);
        System.out.println(this.getName() + " changed state to " + this.getZombieState()+".");
    }


    /** Getter method for zombieType field. */
    public ZombieType getZombieType() {
        ZombieType type =null;
        for(int i=0;i<ZombieType.values().length;i++) {
            if (ZombieType.values()[i].getValue() == this.zombieType) {
                type = ZombieType.values()[i];
            }
        }
        return type;
    }


    /** Getter method for collisionRange field. */
    public double getCollisionRange() {
        return collisionRange;
    }

    /** Getter method for detectionRange field. */
    public double getDetectionRange() {
        return detectionRange;
    }

    /** Getter method for zombieState field. */
    public ZombieState getZombieState() {
        return zombieState;
    }

    /** Setter method for zombieState field. */
    public void setZombieState(ZombieState zombieState) {
        this.zombieState = zombieState;
    }

}
