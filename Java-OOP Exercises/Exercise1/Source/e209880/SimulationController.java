package e209880;
import java.util.ArrayList;
import java.util.List;

/** Represents our virtual simulation world.
 * It contains dimensions of simulation world as fields, and
 * contains controller methods to administrate the environment. */
public class SimulationController {
    private final double height;
    private final double width;
    List<SimulationObject> zombies = new ArrayList<SimulationObject>();
    List<SimulationObject> soldiers = new ArrayList<SimulationObject>();
    List<SimulationObject> bullets = new ArrayList<SimulationObject>();
    List<SimulationObject> addedBullets = new ArrayList<SimulationObject>();
    List<SimulationObject> removedEntities = new ArrayList<SimulationObject>();

    /** Constructor for a new simulation controller.
     * It initializes height and weight of environment. */
    public SimulationController(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /** Getter method for height field. */
    public double getHeight() {
        return height;
    }

    /** Getter method for width field. */
    public double getWidth() {
        return width;
    }


    /** Simulates all participants for one step in the environment.
     * It also administrates the addition and removal operation for participants as a result of their step functions.  */
    public void stepAll() {
        int i;
        for(i=0;i<soldiers.size();i++) {
            if (soldiers.get(i).isActive()) {
                soldiers.get(i).step(this);
            }
        }
        for(i=0;i<zombies.size();i++) {
            if (zombies.get(i).isActive()) {
                zombies.get(i).step(this);
            }
        }
        for(i=0;i<bullets.size();i++) {
            if (bullets.get(i).isActive()) {
                bullets.get(i).step(this);
            }
        }
        for(i=0;i<addedBullets.size();i++) {
            bullets.add(addedBullets.get(i));
        }
        addedBullets.clear();

        for(i=0;i<removedEntities.size();i++) {
        	removeSimulationObject(removedEntities.get(i));
        }
        removedEntities.clear();

        for(i=0;i<bullets.size();) {
            if (!bullets.get(i).isActive()) {
                bullets.remove(i);
            }
            else {
            	break;
            }
        }
    }


    /** Adds the simulation object given in the parameter to the simulation. */
    public void addSimulationObject(SimulationObject obj) {
        if (obj instanceof Zombie) {
            zombies.add(obj);
        }
        if (obj instanceof Soldier) {
            soldiers.add(obj);
        }
        if (obj instanceof Bullet) {
            bullets.add(obj);
        }
    }

    /** Removes the simulation object given in the parameter from the simulation. */
    public void removeSimulationObject(SimulationObject obj) {
        if (obj instanceof Zombie) {
            zombies.remove(obj);
        }
        if (obj instanceof Soldier) {
            soldiers.remove(obj);
        }
        if (obj instanceof Bullet) {
            bullets.remove(obj);
        }
    }



    /** Checks whether there are only zombies or soldiers or none of them left in the simulation.
     * If this is the case, returns true. */
    public boolean isFinished() {

        if (zombies.size() != 0 && soldiers.size() == 0) {
            return true;
        }
        if (zombies.size() == 0 && soldiers.size() != 0) {
            return true;
        }
        if(zombies.size() == 0 && soldiers.size() == 0) {
            return true;
        }


        return false;

    }
}

