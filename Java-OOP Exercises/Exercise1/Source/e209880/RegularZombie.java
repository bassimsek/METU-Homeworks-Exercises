package e209880;
/** A class that represents real-world Regular Zombie objects
 * for our environment. Regular Zombies are kind of Zombies.
 */
public class RegularZombie extends Zombie {

    private int stepCountInFollowing =0;

    /** Constructor for Regular Zombie object.
     * Its name and position are initialized. */
    public RegularZombie(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,5.0,2.0,2.0,20.0);
    }


    /** Getter method for stepCountInFollowing field. */
    public int getStepCountInFollowing() {
        return stepCountInFollowing;
    }


    /** Setter method for stepCountInFollowing field. */
    public void setStepCountInFollowing(int stepCountInFollowing) {
        this.stepCountInFollowing = stepCountInFollowing;
    }


    /** Simulates regular zombie object's behavior for that step. */
    public void step(SimulationController controller) {
        if (!super.stepInit(controller)) {
            SimulationObject closestSoldier = findClosestEntity(controller.soldiers);
            if (this.getZombieState() == ZombieState.WANDERING) {
                outOfBoundTask(controller);
                if (isClosestSoldierInDetectionRange(closestSoldier)) {
                    changeDirectionToEntity(closestSoldier);
                    changeStateTo(ZombieState.FOLLOWING);
                }
                this.setStepCountInFollowing(0);
            } else if (this.getZombieState() == ZombieState.FOLLOWING) {
                this.setStepCountInFollowing(this.getStepCountInFollowing()+1);
                outOfBoundTask(controller);
                if (this.getStepCountInFollowing() == 4) {
                    changeStateTo(ZombieState.WANDERING);
                    this.setStepCountInFollowing(0);
                }
            }
        }
    }

}
