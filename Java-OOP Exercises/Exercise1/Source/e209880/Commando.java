package e209880;
/** A class that represents real-world Commando objects
 * for our environment. Commandos are kind of Soldiers.
 */
public class Commando extends Soldier {

    /** Constructor for Commando object.
     * Its name and position are initialized. */
    public Commando(String name, Position position) { // DO NOT CHANGE PARAMETERS
        super(name,position,10.0,2.0,2.0,10.0);
    }



    /** Simulates commando object's behavior for that step. */
    public void step(SimulationController controller) {
        super.firstStep();
        SimulationObject closestZombie = findClosestEntity(controller.zombies);
        if (this.getSoldierState() == SoldierState.SEARCHING) {
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeDirectionToEntity(closestZombie);
                changeStateTo(SoldierState.SHOOTING);
                return;
            }
            outOfBoundTask(controller);
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeDirectionToEntity(closestZombie);
                changeStateTo(SoldierState.SHOOTING);
            }
        } else if (this.getSoldierState() == SoldierState.SHOOTING) {
            createBullet(40.0,controller);
            if (isClosestZombieInShootingRange(closestZombie)) {
                changeDirectionToEntity(closestZombie);
            } else {
                changeDirectionToRandom();
                changeStateTo(SoldierState.SEARCHING);
            }
        }
    }

}
