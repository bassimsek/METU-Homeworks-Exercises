
public class Seat {

    private String name;
    private User ownerUser;
    private boolean isTaken;


    public Seat(String name) {
        this.name = name;
        this.isTaken = false;
        this.ownerUser = null;
    }


    public boolean isTaken() {
        return isTaken;
    }

    public synchronized void setAsTaken(User ownerUser) {
        if (this.isTaken == false) {
            this.isTaken = true;
            this.ownerUser = ownerUser;
        }
    }

    public String getOwnerUserName() {
        return ownerUser.getName();
    }


    @Override
    public String toString() {
        return name;
    }
}
