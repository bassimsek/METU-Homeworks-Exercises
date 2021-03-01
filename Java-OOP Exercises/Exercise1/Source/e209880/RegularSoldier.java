package e209880;
/** A class that represents real-world Regular Soldier objects
 * for our environment. Regular Soldiers are kind of Soldiers.
 */
public class RegularSoldier extends Soldier {


    /** Constructor for Regular Soldier object.
     * Its name and position are initialized. */
    public RegularSoldier(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,5.0,1.0,2.0,20.0);
    }




    /** Simulates regular soldier object's behavior for that step. */
    public void step(SimulationController controller) {
        super.firstStep();
        SimulationObject closestZombie = findClosestEntity(controller.zombies);
        if (this.getSoldierState() == SoldierState.SEARCHING) {
            outOfBoundTask(controller);
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeStateTo(SoldierState.AIMING);
            }
        } else if (this.getSoldierState() == SoldierState.AIMING) {
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeDirectionToEntity(closestZombie);
                changeStateTo(SoldierState.SHOOTING);
            } else {
                changeStateTo(SoldierState.SEARCHING);
            }
        } else if (this.getSoldierState() == SoldierState.SHOOTING) {
            createBullet(40.0,controller);
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeStateTo(SoldierState.AIMING);
            } else {
                changeDirectionToRandom();
                changeStateTo(SoldierState.SEARCHING);
            }
        }
    }

}


