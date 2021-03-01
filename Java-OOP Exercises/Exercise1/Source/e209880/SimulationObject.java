package e209880;
import java.util.List;

/** Top most ancestor class that represents any
 * object in the simulation environment. It contains common
 * state fields and behavioral methods for all simulation objects.
 */
public abstract class SimulationObject {
    private final String name;
    private Position position;
    private Position direction;
    private final double speed;
    private boolean active;
    private boolean isFirstStepCall = true;

    /** Constructor for creating any simulation object.
     * Common state fields are initialized. */
    public SimulationObject(String name, Position position, double speed) {
        this.name = name;
        this.position = position;
        this.speed = speed;
        this.direction = null;
        this.active = true;
    }

    /** Generates random direction for simulation object
     * if object is in its first step in the environment. */
    public void firstStep() {
        if (isFirstStepCall) {
            changeDirectionToRandom();
            isFirstStepCall = false;
        }
    }

    /** Finds the closest entity(it can be zombie,soldier or bullet according to given entities list parameter) in the environment. */
    public SimulationObject findClosestEntity(List<SimulationObject> entities) {
        Position currentEntityPosition = null;
        double closestDistance = Double.MAX_VALUE;
        double currentDistance;
        SimulationObject closestEntity = null;
        for (int j = 0; j < entities.size(); j++) {
        	if (entities.get(j).isActive()) {
        		currentEntityPosition = entities.get(j).getPosition();
            	currentDistance = this.getPosition().distance(currentEntityPosition);
            	if (currentDistance < closestDistance) {
                	closestDistance = currentDistance;
                	closestEntity = entities.get(j);
            	}
        	}  
        }
        return closestEntity;
    }

    /** Changes object's direction to the given entity. */
    public void changeDirectionToEntity(SimulationObject closestEntity) {
        this.getDirection().setX(closestEntity.getPosition().getX() - this.getPosition().getX());
        this.getDirection().setY(closestEntity.getPosition().getY() - this.getPosition().getY());
        this.getDirection().setLength();
        this.getDirection().normalize();
        System.out.println(this.getName() + " changed direction to " + this.getDirection() + ".");
    }

    /** Generates new random direction for object. */
    public void changeDirectionToRandom () {
        this.setDirection(Position.generateRandomDirection(true));
        System.out.println(this.getName() + " changed direction to " + this.getDirection() + ".");
    }

    /** Takes the object to its next position in the environment */
    private void goToNextPosition () {
        this.getPosition().add(new Position(this.getDirection().getX()*this.getSpeed(), this.getDirection().getY()*this.getSpeed()));
        System.out.println(this.getName() + " moved to " + this.getPosition() + ".");
    }

    /** Calculates the next position of object.
     * If next position is out of bounds of environment, it generates new random direction for object.
     * If not, it takes the object to its next position.*/
    public void outOfBoundTask (SimulationController controller) {
        if (this.getPosition().getX() + (this.getDirection().getX()*this.getSpeed()) > controller.getWidth() || this.getPosition().getY() + (this.getDirection().getY()*this.getSpeed()) > controller.getHeight() || this.getPosition().getX() + (this.getDirection().getX()*this.getSpeed()) <0 || this.getPosition().getY() + (this.getDirection().getY()*this.getSpeed()) < 0) {
            changeDirectionToRandom();
        } else {
            goToNextPosition();
        }
    }

    /** Getter method for name field. */
    public String getName() {
        return name;
    }

    /** Getter method for position field. */
    public Position getPosition() { return position; }

    /** Setter method for position field. */
    public void setPosition(Position position) {
        this.position = position;
    }

    /** Getter method for direction field. */
    public Position getDirection() {
        return direction;
    }

    /** Setter method for direction field. */
    public void setDirection(Position direction) {
        this.direction = direction;
    }

    /** Getter method for speed field. */
    public double getSpeed() {
        return speed;
    }

    /** Getter method for active field. */
    public boolean isActive() {
        return active;
    }

    /** Setter method for active field. */
    public void setActive(boolean active) {
        this.active = active;
    }


    /** Step function for an object in the simulation.
     * It simulates object's behavior under different circumstances for that step. */
    public abstract void step(SimulationController controller);
}
