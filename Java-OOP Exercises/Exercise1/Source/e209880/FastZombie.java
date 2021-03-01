package e209880;
/** A class that represents real-world Fast Zombie objects
 * for our environment. Fast Zombies are kind of Zombies.
 */
public class FastZombie extends Zombie {



    /** Constructor for Fast Zombie object.
     * Its name and position are initialized. */
    public FastZombie(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,20.0,3.0,2.0,20.0);
    }



    /** Simulates fast zombie object's behavior for that step. */
    public void step(SimulationController controller) {
        if (!super.stepInit(controller)) {
            SimulationObject closestSoldier = findClosestEntity(controller.soldiers);
            if (this.getZombieState() == ZombieState.WANDERING) {
                if (isClosestSoldierInDetectionRange(closestSoldier)) {
                    changeDirectionToEntity(closestSoldier);
                    changeStateTo(ZombieState.FOLLOWING);
                    return;
                }
                outOfBoundTask(controller);
            } else if (this.getZombieState() == ZombieState.FOLLOWING) {
                outOfBoundTask(controller);
                changeStateTo(ZombieState.WANDERING);
            }
        }
    }

}
