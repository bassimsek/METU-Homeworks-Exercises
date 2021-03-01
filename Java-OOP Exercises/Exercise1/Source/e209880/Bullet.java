package e209880;
/** A class that represents real-world Bullet objects
 * for our environment.
 */
public class Bullet extends SimulationObject {


    private static int bulletNo = 0;

    /** Constructor for Bullet object.
     * Bullet's name with its number,position and speed are initialized. */
    public Bullet(Position position,double speed) {
        super("Bullet"+bulletNo,position,speed);
        bulletNo++;
    }


    /** Simulates the bullet object's behavior for that step. */
    public void step(SimulationController controller) {
        int n = (int) (this.getSpeed() /1.0);
        SimulationObject closestZombie = findClosestEntity(controller.zombies);
        for (int i=0;i<n;i++) {
            if (this.getPosition().distance(closestZombie.getPosition()) <= ((Zombie) closestZombie).getCollisionRange()) {
                this.setActive(false);
                controller.removedEntities.add(closestZombie);
                System.out.println(this.getName()+ " hit "+ closestZombie.getName()+".");
                return;
            }
            this.getDirection().mult(1.0);
            this.getPosition().add(this.getDirection());

            if (this.getPosition().getX() > controller.getWidth() || this.getPosition().getY() > controller.getHeight() || this.getPosition().getX() < 0 || this.getPosition().getY() < 0) {
                System.out.println(this.getName()+ " moved out of bounds.");
                this.setActive(false);
                return;
            }

        }
        System.out.println(this.getName() + " dropped to the ground at " + this.getPosition() + ".");
        this.setActive(false);
    }

}
