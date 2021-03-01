import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class MyQueue {

    private static Semaphore[] semStorageSpaceForSmelters;
    private static Semaphore[] semTransportFromSmelter;
    private static Semaphore[] semActiveSmelters;

    private static Semaphore[] semRequiredIngots;
    private static Semaphore[] semStorageSpaceForConstructors;
    private static Semaphore[] semActiveConstructors;



    public static void createSmelterSemaphores(int numberOfSmelter, List<Integer> smelterStorageCapacities) {
        int i;
        semStorageSpaceForSmelters = new Semaphore[numberOfSmelter];
        semTransportFromSmelter = new Semaphore[numberOfSmelter];
        semActiveSmelters = new Semaphore[numberOfSmelter];

        for(i = 0; i < numberOfSmelter; i++) {
            semStorageSpaceForSmelters[i] = new Semaphore(smelterStorageCapacities.get(i));
        }
        for(i = 0; i < numberOfSmelter; i++) {
            semTransportFromSmelter[i] = new Semaphore(0);
        }
        for(i = 0; i < numberOfSmelter; i++) {
            semActiveSmelters[i] = new Semaphore(1);
        }
    }

    public static void createConstructorSemaphores(int numberOfConstructor, List<Integer> constructorStorageCapacities) {
        int i;
        semRequiredIngots = new Semaphore[numberOfConstructor];
        semStorageSpaceForConstructors = new Semaphore[numberOfConstructor];
        semActiveConstructors = new Semaphore[numberOfConstructor];

        for(i = 0; i < numberOfConstructor; i++) {
            semRequiredIngots[i] = new Semaphore(0);
        }
        for(i = 0; i < numberOfConstructor; i++) {
            semStorageSpaceForConstructors[i] = new Semaphore(constructorStorageCapacities.get(i));
        }
        for(i = 0; i < numberOfConstructor; i++) {
            semActiveConstructors[i] = new Semaphore(1);
        }
    }



    void waitCanProduce(int smelterId) {
        try {
            semStorageSpaceForSmelters[smelterId-1].acquire();
        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
    }



    void ingotProduced(int smelterId) {

        semTransportFromSmelter[smelterId-1].release();

    }


    void smelterStopped(int smelterId) {
        try {
            semActiveSmelters[smelterId-1].acquire();
        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
    }




    boolean waitIngots(int constructorId, int ingotType) {
        boolean result = true;

        try {
            if (ingotType == 0) {
                result = semRequiredIngots[constructorId-1].tryAcquire(2,3, TimeUnit.SECONDS);

            } else if (ingotType == 1) {
                result = semRequiredIngots[constructorId-1].tryAcquire(3,3, TimeUnit.SECONDS);
            }
        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
        return result;
    }



    void constructorProduced(int constructorId, int ingotType) {
        if (ingotType == 0) {
            semStorageSpaceForConstructors[constructorId-1].release(2);
        }
        if (ingotType == 1) {
            semStorageSpaceForConstructors[constructorId-1].release(3);
        }

    }


    void constructorStopped(int constructorId) {
        try {
            semActiveConstructors[constructorId-1].acquire();
        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }

    }



    boolean isTargetSmelterActive(int smelterID) {
        int availablePermits = semActiveSmelters[smelterID-1].availablePermits();
        if (availablePermits > 0) {
           return true;
        } else {
            return false;
        }
    }

    boolean isTargetSmelterHasIngots(int smelterID) {
        int availablePermits = semTransportFromSmelter[smelterID-1].availablePermits();
        if (availablePermits > 0) {
            return true;
        } else {
            return false;
        }
    }


    boolean isTargetConstructorActive(int constructorID) {
        int availablePermits = semActiveConstructors[constructorID-1].availablePermits();
        if (availablePermits > 0) {
            return true;
        } else {
            return false;
        }
    }


    void controlSmelterStatus(int smelterId) {
        int availablePermits = semTransportFromSmelter[smelterId-1].availablePermits();
        if (!isTargetSmelterActive(smelterId) && availablePermits == 0) {
            semTransportFromSmelter[smelterId-1].release();
        }
    }



    void waitNextLoad(int smelterId) {
        try {
            if (isTargetSmelterActive(smelterId) || isTargetSmelterHasIngots(smelterId) ) {
                semTransportFromSmelter[smelterId - 1].acquire();
            }
        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
    }



    void loaded(int smelterId) {

        semStorageSpaceForSmelters[smelterId-1].release();
    }



    void waitConstructor(int constructorId) {
        try {
            semStorageSpaceForConstructors[constructorId-1].acquire();

        }
        catch(InterruptedException e) {
            System.out.println("InterruptedException caught");
        }
    }



    void unloaded(int constructorId) {

        semRequiredIngots[constructorId - 1].release();
    }


    int targetSmelterStorage(int smelterId) {
        int availablePermits = semStorageSpaceForSmelters[smelterId-1].availablePermits();
        return availablePermits;
    }


}
