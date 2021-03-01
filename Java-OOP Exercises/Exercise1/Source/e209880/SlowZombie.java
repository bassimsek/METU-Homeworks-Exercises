package e209880;
/** A class that represents real-world Slow Zombie objects
 * for our environment. Slow Zombies are kind of Zombies.
 */
public class SlowZombie extends Zombie {

    /** Constructor for Slow Zombie object.
     * Its name and position are initialized. */
    public SlowZombie(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,2.0,1.0,1.0,40.0);
    }



    /** Simulates slow zombie object's behavior for that step. */
    public void step(SimulationController controller) {
        if (!super.stepInit(controller)) {
            SimulationObject closestSoldier = findClosestEntity(controller.soldiers);
            if (this.getZombieState() == ZombieState.WANDERING) {
                if (isClosestSoldierInDetectionRange(closestSoldier)) {
                    changeStateTo(ZombieState.FOLLOWING);
                    return;
                }
                outOfBoundTask(controller);
            } else if (this.getZombieState() == ZombieState.FOLLOWING) {
                if (isClosestSoldierInDetectionRange(closestSoldier)) {
                    changeDirectionToEntity(closestSoldier);
                }
                outOfBoundTask(controller);
                if (isClosestSoldierInDetectionRange(closestSoldier)) {
                    changeStateTo(ZombieState.WANDERING);
                }
            }
        }
    }

}

