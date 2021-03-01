import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;

public class App {

    int numberOfSmelters;
    int numberOfConstructors;
    int numberOfTransporters;
    int threadSize;

    ArrayList<Integer> smelterIntervals = new ArrayList<Integer>();
    ArrayList<Integer> smelterStorageCapacities = new ArrayList<Integer>();
    ArrayList<Integer> ingotTypeOfSmelters = new ArrayList<Integer>();
    ArrayList<Integer> maxProductionFromSmelters = new ArrayList<Integer>();

    ArrayList<Integer> constructorIntervals = new ArrayList<Integer>();
    ArrayList<Integer> constructorStorageCapacities = new ArrayList<Integer>();
    ArrayList<Integer> ingotTypeOfConstructors = new ArrayList<Integer>();

    ArrayList<Integer> transporterIntervals = new ArrayList<Integer>();
    ArrayList<Integer> targetSmelterIDs = new ArrayList<Integer>();
    ArrayList<Integer> targetConstructorIDs = new ArrayList<Integer>();



    public App(String inputFileName) {
        int i;
        readInputFile(inputFileName);


        MyQueue q  =  new MyQueue();
        q.createSmelterSemaphores(numberOfSmelters, smelterStorageCapacities);
        q.createConstructorSemaphores(numberOfSmelters, constructorStorageCapacities);

        HW2Logger.InitWriteOutput();
        ExecutorService taskList = Executors.newFixedThreadPool(threadSize);


        for (i = 0;i < numberOfSmelters; i++) {
            taskList.execute(new Smelter(i+1, smelterIntervals.get(i), smelterStorageCapacities.get(i), ingotTypeOfSmelters.get(i), maxProductionFromSmelters.get(i), q));
        }
        for (i = 0;i < numberOfConstructors; i++) {
            taskList.execute(new Constructor(i+1, constructorIntervals.get(i), constructorStorageCapacities.get(i), ingotTypeOfConstructors.get(i), q));
        }
        for (i = 0;i < numberOfTransporters; i++) {
            taskList.execute(new Transporter(i+1, transporterIntervals.get(i), targetSmelterIDs.get(i), targetConstructorIDs.get(i), q));
        }
        taskList.shutdown();

    }


    public void readInputFile(String fileName) {
        int lineCounter = 1;
        String[] specifications;

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            numberOfSmelters = Integer.parseInt(line);
            lineCounter = numberOfSmelters;
            line = reader.readLine();


            while (lineCounter != 0 && line != null) {
                specifications = line.split(" ");
                smelterIntervals.add(Integer.parseInt(specifications[0]));
                smelterStorageCapacities.add(Integer.parseInt(specifications[1]));
                ingotTypeOfSmelters.add(Integer.parseInt(specifications[2]));
                maxProductionFromSmelters.add(Integer.parseInt(specifications[3]));

                line = reader.readLine();
                lineCounter--;
            }

            numberOfConstructors = Integer.parseInt(line);
            lineCounter = numberOfConstructors;
            line = reader.readLine();


            while (lineCounter != 0 && line != null) {
                specifications = line.split(" ");
                constructorIntervals.add(Integer.parseInt(specifications[0]));
                constructorStorageCapacities.add(Integer.parseInt(specifications[1]));
                ingotTypeOfConstructors.add(Integer.parseInt(specifications[2]));

                line = reader.readLine();
                lineCounter--;
            }

            numberOfTransporters = Integer.parseInt(line);
            lineCounter = numberOfTransporters;
            line = reader.readLine();


            while (lineCounter != 0 && line != null) {
                specifications = line.split(" ");
                transporterIntervals.add(Integer.parseInt(specifications[0]));
                targetSmelterIDs.add(Integer.parseInt(specifications[1]));
                targetConstructorIDs.add(Integer.parseInt(specifications[2]));

                line = reader.readLine();
                lineCounter--;
            }

            threadSize = numberOfSmelters + numberOfConstructors + numberOfTransporters;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pauseAtInterval(int interval) {
        try {
            Random random = new Random(System.currentTimeMillis());
            DoubleStream stream;
            stream = random.doubles(1, interval-interval*0.01, interval+interval*0.02);
            Thread.sleep((long) stream.findFirst().getAsDouble());
        } catch (InterruptedException ie) {
        }
    }




    private class Smelter implements Runnable {
        private final int id;
        private final int interval;
        private final int storageCapacity;
        private final int ingotType;
        private int totalProduction;
        MyQueue q;


        public Smelter(int id, int interval, int storageCapacity, int ingotType, int totalProduction, MyQueue q) {
            this.id = id;
            this.interval = interval;
            this.storageCapacity = storageCapacity;
            this.ingotType = ingotType;
            this.totalProduction = totalProduction;
            this.q = q;
        }

        public void run() {
            HW2Logger.WriteOutput(id, 0, 0, Action.SMELTER_CREATED);

            while(totalProduction > 0) {
                q.waitCanProduce(id);
                HW2Logger.WriteOutput(id, 0, 0, Action.SMELTER_STARTED);
                pauseAtInterval(this.interval);
                q.ingotProduced(id);
                HW2Logger.WriteOutput(id, 0, 0, Action.SMELTER_FINISHED);
                pauseAtInterval(this.interval);
                this.totalProduction--;
            }

            q.smelterStopped(id);
            HW2Logger.WriteOutput(id, 0, 0, Action.SMELTER_STOPPED);
        }
    }


    private class Constructor implements Runnable {
        private final int id;
        private final int interval;
        private final int storageCapacity;
        private final int ingotType;
        MyQueue q;


        public Constructor(int id, int interval, int storageCapacity, int ingotType, MyQueue q) {
            this.id = id;
            this.interval = interval;
            this.storageCapacity = storageCapacity;
            this.ingotType = ingotType;
            this.q = q;
        }


        public void run() {
            HW2Logger.WriteOutput(0, 0, id, Action.CONSTRUCTOR_CREATED);
            boolean timeoutNotElapsed;

            while(true) {
                timeoutNotElapsed = q.waitIngots(id, ingotType);
                if(!timeoutNotElapsed) {
                    break;
                }

                HW2Logger.WriteOutput(0, 0, id, Action.CONSTRUCTOR_STARTED);
                pauseAtInterval(this.interval);

                q.constructorProduced(id, ingotType);
                HW2Logger.WriteOutput(0, 0, id, Action.CONSTRUCTOR_FINISHED);
                // Below sleep is added according to Cagri Utku Akpak's post on OdtuClass although there is no sleep here in the homework text.
                pauseAtInterval(this.interval);

            }
            q.constructorStopped(id);
            HW2Logger.WriteOutput(0, 0, id, Action.CONSTRUCTOR_STOPPED);
        }
    }




    private class Transporter implements Runnable {
        private final int id;
        private final int interval;
        private final int targetSmelterID;
        private final int targetConstructorID;
        MyQueue q;


        public Transporter(int id, int interval, int targetSmelterID, int targetConstructorID, MyQueue q) {
            this.id = id;
            this.interval = interval;
            this.targetSmelterID = targetSmelterID;
            this.targetConstructorID = targetConstructorID;
            this.q = q;
        }

        public void run() {
            HW2Logger.WriteOutput(0, id, 0, Action.TRANSPORTER_CREATED);

            while((q.isTargetSmelterActive(targetSmelterID) || q.isTargetSmelterHasIngots(targetSmelterID)) && q.isTargetConstructorActive(targetConstructorID)) {

                q.waitNextLoad(targetSmelterID);
                /* If there is any other transporter tries to acquire in waitNextLoad() function when target smelter is inactive or
                has no ingot in its storage, I added below if block to save that waiting transporter. Otherwise that transporter may be in stuck
                in waitNextLoad() function, and can not terminate properly according to my own tests. Thus, this if block is necessary for my implementation I think.
                 */
                if (smelterStorageCapacities.get(targetSmelterID-1) == q.targetSmelterStorage(targetSmelterID)) {
                    break;
                }
                HW2Logger.WriteOutput(targetSmelterID, id, 0, Action.TRANSPORTER_TRAVEL);
                pauseAtInterval(this.interval);
                HW2Logger.WriteOutput(targetSmelterID, id, 0, Action.TRANSPORTER_TAKE_INGOT);
                pauseAtInterval(this.interval);
                q.loaded(targetSmelterID);
                q.waitConstructor(targetConstructorID);
                HW2Logger.WriteOutput(0, id, targetConstructorID, Action.TRANSPORTER_TRAVEL);
                pauseAtInterval(this.interval);
                HW2Logger.WriteOutput(0, id, targetConstructorID, Action.TRANSPORTER_DROP_INGOT);
                pauseAtInterval(this.interval);
                q.unloaded(targetConstructorID);
            }
            HW2Logger.WriteOutput(0, id, 0, Action.TRANSPORTER_STOPPED);

            if (!q.isTargetSmelterActive(targetSmelterID) && !q.isTargetSmelterHasIngots(targetSmelterID)) {
                q.controlSmelterStatus(targetSmelterID);
            }
        }
    }


}

