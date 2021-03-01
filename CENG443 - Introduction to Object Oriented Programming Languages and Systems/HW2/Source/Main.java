import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


public class Main {
    public static void main(String[] args) {

        ArrayList<ArrayList<Seat>> grid;
        List<User> userList;

        // Reading input from stdin
        BufferedReader myReader = InputOutputHelper.getReader();
        grid = InputOutputHelper.createGrid(myReader);

        ReentrantLock rel = new ReentrantLock();
        userList = InputOutputHelper.createUsers(myReader,grid,rel);
        InputOutputHelper.closeReader(myReader);


        // Creating user threads
        Logger.InitLogger();
        ExecutorService executor = Executors.newFixedThreadPool(userList.size());
        for(int i=0; i<userList.size();i++) {
            executor.execute(userList.get(i));
        }
        executor.shutdown();


        // Waiting all threads to finish their executions
        boolean finished = false;
        try {
            finished = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Print results after each thread finishes its execution
        if(finished) {
            InputOutputHelper.printResult(grid);
        }

    }

}
