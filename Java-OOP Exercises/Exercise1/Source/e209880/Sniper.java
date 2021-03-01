package e209880;
/** A class that represents real-world Sniper objects
 * for our environment. Snipers are kind of Soldiers.
 */
public class Sniper extends Soldier {


    /** Constructor for Sniper object.
     * Its name and position are initialized. */
    public Sniper(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,2.0,3.0,5.0,40.0);
    }




    /** Simulates sniper object's behavior for that step. */
    public void step(SimulationController controller) {
        super.firstStep();
        SimulationObject closestZombie = findClosestEntity(controller.zombies);
        if (this.getSoldierState() == SoldierState.SEARCHING) {
            outOfBoundTask(controller);
            changeStateTo(SoldierState.AIMING);
        } else if (this.getSoldierState() == SoldierState.AIMING) {
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeDirectionToEntity(closestZombie);
                changeStateTo(SoldierState.SHOOTING);
            } else {
                changeStateTo(SoldierState.SEARCHING);
            }
        } else if (this.getSoldierState() == SoldierState.SHOOTING) {
            createBullet(100.0,controller);
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeStateTo(SoldierState.AIMING);
            } else {
                changeDirectionToRandom();
                changeStateTo(SoldierState.SEARCHING);
            }
        }
    }

}
