import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class InputOutputHelper {

    private static String[] alphabet = {"A","B","C","D","E","F","G","H","I","J","K",
            "L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};


    public static BufferedReader getReader() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader;
    }


    public static ArrayList<ArrayList<Seat>> createGrid(BufferedReader reader) {

        ArrayList<ArrayList<Seat>> grid =  new ArrayList<ArrayList<Seat>>();
        int n,m;

        try {
            String line = reader.readLine();
            String[] gridSizes = line.split(" ");
            n = Integer.parseInt(gridSizes[0]);
            m = Integer.parseInt(gridSizes[1]);
            // Creating our NxM grid
            for(int a=0 ; a<n ; a++) {
                ArrayList<Seat> row = new ArrayList<Seat>();
                grid.add(row);
                for(int b=0; b<m ; b++) {
                    String seatName = alphabet[a] + b;
                    Seat seat = new Seat(seatName);
                    grid.get(a).add(seat);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return grid;
    }


    public static List<User> createUsers(BufferedReader reader, ArrayList<ArrayList<Seat>> grid, ReentrantLock rel) {

        List<User> userList = new ArrayList<User>();

        try {
            String line = reader.readLine();
            int numberOfUsers = Integer.parseInt(line);

            Map<String,Integer> seatInfo = new HashMap<String, Integer>();
            for(int x=0; x<alphabet.length ;x++) {
                seatInfo.put(alphabet[x],x);
            }

            for(int i=0;i<numberOfUsers;i++) {
                line = reader.readLine();
                String[] currentUserInfo = line.split(" ");
                String userName = currentUserInfo[0];
                List<Seat> wantedSeatsFromUser = new ArrayList<>();
                for(int j=1;j<currentUserInfo.length;j++) {
                    String[] currentSeatInfo = currentUserInfo[j].split("");
                    wantedSeatsFromUser.add(grid.get(seatInfo.get(currentSeatInfo[0])).get(Integer.parseInt(currentSeatInfo[1])));
                }
                User currentUser = new User(userName,wantedSeatsFromUser,rel);
                userList.add(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userList;
    }


    public static void closeReader(BufferedReader reader) {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void printResult(ArrayList<ArrayList<Seat>> grid) {
        for(int i=0; i<grid.size(); i++) {
            for(int j=0; j< grid.get(i).size(); j++) {
                if (grid.get(i).get(j).isTaken()) {
                    System.out.printf("T:%s", grid.get(i).get(j).getOwnerUserName());
                } else {
                    System.out.printf("E:%s", "");
                }
                if ((j+1) != grid.get(i).size()) {
                    System.out.printf(" ");
                }
            }
            System.out.printf("%n");
        }
    }

}
