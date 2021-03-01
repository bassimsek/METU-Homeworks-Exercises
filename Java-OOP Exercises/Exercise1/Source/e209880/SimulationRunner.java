package e209880;
import java.util.logging.Level;
import java.util.logging.Logger;
/** Runner class that includes main function and run the environment. */
public class SimulationRunner {

    public static void main(String[] args) {
        SimulationController simulation = new SimulationController(50, 50);


        simulation.addSimulationObject(new Sniper("Soldier1", new Position(10, 10)));
        simulation.addSimulationObject(new FastZombie("Zombie1", new Position(40, 40)));

        /*simulation.addSimulationObject(new RegularSoldier("Soldier2", new Position(10, 10)));
        simulation.addSimulationObject(new RegularSoldier("Soldier3", new Position(10, 10)));
        simulation.addSimulationObject(new Commando("Soldier4", new Position(10, 10)));
        simulation.addSimulationObject(new SlowZombie("Zombie2", new Position(40, 40)));
        simulation.addSimulationObject(new Sniper("Soldier5", new Position(10, 10)));
        simulation.addSimulationObject(new FastZombie("Zombie3", new Position(40, 40)));*/


        while (!simulation.isFinished()) {
            simulation.stepAll();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimulationRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
