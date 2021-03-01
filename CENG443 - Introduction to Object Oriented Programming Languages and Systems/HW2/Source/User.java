

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class User implements Runnable {

    private final String name;
    private final List<Seat> wantedSeats;
    private ReentrantLock rel;

    public User(String name, List<Seat> wantedSeats, ReentrantLock rel) {
        this.name = name;
        this.wantedSeats = wantedSeats;
        this.rel = rel;
    }

    public String getName() {
        return this.name;
    }


    public boolean areAllWantedSeatsEmpty(List<Seat> wantedSeats) {
        boolean allWantedSeatsAreEmpty = true;

        for(int i=0; i<wantedSeats.size(); i++) {
            if (wantedSeats.get(i).isTaken()) {
                allWantedSeatsAreEmpty = false;
            }
        }

        return allWantedSeatsAreEmpty;
    }


    @Override
    public void run() {
        boolean shouldRetry = true;
        int retryNo = 1;

        try {
            while(shouldRetry) {
                boolean allWantedSeatsAreTaken = false;

                if(areAllWantedSeatsEmpty(wantedSeats)) {
                    double randomNumber = Math.random();
                    if (randomNumber > 0.1) {
                        rel.lock();
                        if (areAllWantedSeatsEmpty(wantedSeats)) {
                            for (int i = 0; i < wantedSeats.size(); i++) {
                                wantedSeats.get(i).setAsTaken(this);
                            }
                            allWantedSeatsAreTaken = true;
                        }
                        rel.unlock();

                        if (allWantedSeatsAreTaken) {
                            Thread.sleep(50);
                            Logger.LogSuccessfulReservation(this.name, wantedSeats.toString(), System.nanoTime(), "Retry No: " + retryNo);
                            break;
                        }
                    } else {
                        Logger.LogDatabaseFailiure(this.name, wantedSeats.toString(), System.nanoTime(),"Failed, trying again.");
                        Thread.sleep(100);
                    }
                } else {
                    Logger.LogFailedReservation(this.name, wantedSeats.toString(), System.nanoTime(),"Seats are not available");
                    shouldRetry = false;
                }
                retryNo++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
