package ceng.ceng351.labdb;

public class Evaluate {

    public static void main(String[] args) {
        LabDB labdb = new LabDB(4);
        try {
            labdb.enter("e4");
            labdb.printLab();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

