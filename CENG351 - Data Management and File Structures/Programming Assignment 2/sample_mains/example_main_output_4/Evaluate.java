package ceng.ceng351.labdb;

public class Evaluate {

    public static void main(String[] args) {
        LabDB labdb = new LabDB(4);
        try {

            labdb.enter("e4");
            labdb.enter("e12");
            labdb.enter("e32");
            labdb.enter("e16");
            labdb.enter("e1");
            labdb.enter("e5");
            labdb.enter("e21");
            labdb.enter("e10");
            labdb.enter("e15");
            labdb.enter("e7");
            labdb.enter("e19");
            labdb.enter("e13");
            labdb.enter("e20");
            labdb.enter("e9");
            labdb.printLab();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

