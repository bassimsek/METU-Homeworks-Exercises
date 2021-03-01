package ceng.ceng351.labdb;


import java.util.*;


public class LabDB {

    private int globalDepth;
    private int bucketSize;
    private Map<String, Bucket> directory;

    

    public LabDB(int bucketSize) {
        globalDepth = 1;
        this.bucketSize = bucketSize;
        this.directory = new HashMap<String, Bucket>();

        Bucket bucket1 = new Bucket(1,bucketSize,true);
        Bucket bucket2 = new Bucket(1,bucketSize,true);

        directory.put("0", bucket1);
        directory.put("1", bucket2);
    }


    
    public void enter(String studentID) {

        String searchKey = search(studentID);

        if(searchKey.equals("-1")) {
            int numericStudentId = Integer.parseInt(studentID.substring(1));

            String binaryOfStudentId = Integer.toBinaryString(numericStudentId);

            int i = 0;
            while(true) {
                if (i == globalDepth) {
                    String hashSuffix = binaryOfStudentId.substring(binaryOfStudentId.length()-i);

                    if(directory.get(hashSuffix).getRecords() != null && directory.get(hashSuffix).getNumberOfElements() < bucketSize) {
                        directory.get(hashSuffix).insert(studentID);
                        break;
                    } else if (directory.get(hashSuffix).getRecords() == null && directory.get(hashSuffix).getLocalDepth() < this.globalDepth) {
                        int local = directory.get(hashSuffix).getLocalDepth();

                        String imageSplitHashSuffix = hashSuffix.substring(hashSuffix.length()-local);
                        for(int c=0; c< globalDepth-local ; c++) {
                            imageSplitHashSuffix = "0" + imageSplitHashSuffix;
                        }
                        
                        if (directory.get(imageSplitHashSuffix).getNumberOfElements() < bucketSize) {
                            directory.get(imageSplitHashSuffix).insert(studentID);
                            break;
                        } else {
                            local++;
                            String newHashSuffix = hashSuffix.substring(hashSuffix.length()-local);
                            for(int c=0; c< globalDepth-local ; c++) {
                                newHashSuffix = "0" + newHashSuffix;
                            }

                            directory.get(newHashSuffix).createBucket();
                            directory.get(newHashSuffix).incrementLocalDepth();
                            directory.get(imageSplitHashSuffix).incrementLocalDepth();

                            for(int x =0; x < directory.get(imageSplitHashSuffix).getRecords().size() ; x++) {
                                String currStudent = directory.get(imageSplitHashSuffix).getRecords().get(x).substring(1);
                                String currentRecord = Integer.toBinaryString(Integer.parseInt(currStudent));
                                int iter = globalDepth-currentRecord.length();
                                for(int y=0;y<iter;y++) {
                                    currentRecord = "0" + currentRecord;
                                }
                                currentRecord = currentRecord.substring(currentRecord.length()-local);
                                if (newHashSuffix.substring(newHashSuffix.length()-local).equals(currentRecord)) {
                                    directory.get(newHashSuffix).insert(directory.get(imageSplitHashSuffix).getRecords().get(x));
                                    directory.get(imageSplitHashSuffix).getRecords().set(x,"!");
                                }
                            }

                            for(int k=0;k<directory.get(imageSplitHashSuffix).getRecords().size();) {
                                if (directory.get(imageSplitHashSuffix).getRecords().get(k).equals("!")) {
                                    directory.get(imageSplitHashSuffix).getRecords().remove(k);
                                } else {
                                    k++;
                                }
                            }


                            if (newHashSuffix.equals(hashSuffix) && directory.get(newHashSuffix).getNumberOfElements() < bucketSize) {
                                directory.get(newHashSuffix).insert(studentID);
                                break;
                            } else if (directory.get(hashSuffix).getNumberOfElements() < bucketSize) {
                                directory.get(hashSuffix).insert(studentID);
                                break;
                            } else {
                                continue;
                            }


                        }
                    } else if (directory.get(hashSuffix).getRecords() != null && directory.get(hashSuffix).getLocalDepth() < this.globalDepth) {
                        int local = directory.get(hashSuffix).getLocalDepth();
                        if (directory.get(hashSuffix).getNumberOfElements() < bucketSize) {
                            directory.get(hashSuffix).insert(studentID);
                            break;
                        } else {

                            String newHashSuffix = hashSuffix.substring(hashSuffix.length()-local);
                            for(int c=0; c< globalDepth-local ; c++) {
                                if (c==0) {
                                    newHashSuffix = "1" + newHashSuffix;
                                } else {
                                    newHashSuffix = "0" + newHashSuffix;
                                }
                            }

                            if (directory.get(newHashSuffix).getRecords() == null) {
                                directory.get(newHashSuffix).createBucket();
                            }

                            directory.get(newHashSuffix).incrementLocalDepth();
                            directory.get(hashSuffix).incrementLocalDepth();
                            local++;


                            for(int x =0; x <directory.get(hashSuffix).getRecords().size() ; x++) {
                                String currStudent = directory.get(hashSuffix).getRecords().get(x).substring(1);
                                String currentRecord = Integer.toBinaryString(Integer.parseInt(currStudent));
                                int iter = globalDepth-currentRecord.length();
                                for(int y=0;y<iter;y++) {
                                    currentRecord = "0" + currentRecord;
                                }

                                currentRecord = currentRecord.substring(currentRecord.length()-local);
                                if (newHashSuffix.substring(newHashSuffix.length()-local).equals(currentRecord)) {
                                    directory.get(newHashSuffix).insert(directory.get(hashSuffix).getRecords().get(x));
                                    directory.get(hashSuffix).getRecords().set(x,"!");
                                }
                            }

                            for(int k=0;k<directory.get(hashSuffix).getRecords().size();) {
                                if (directory.get(hashSuffix).getRecords().get(k).equals("!")) {
                                    directory.get(hashSuffix).getRecords().remove(k);
                                } else {
                                    k++;
                                }
                            }

                            if (newHashSuffix.equals(hashSuffix) && directory.get(newHashSuffix).getNumberOfElements() < bucketSize) {
                                directory.get(newHashSuffix).insert(studentID);
                                break;
                            } else if (directory.get(hashSuffix).getNumberOfElements() < bucketSize) {
                                directory.get(hashSuffix).insert(studentID);
                                break;
                            } else {
                                continue;
                            }

                        }
                    }
                    else if (directory.get(hashSuffix).getLocalDepth() ==  this.globalDepth) {
                        globalDepth++;
                        
                        directory.get(hashSuffix).incrementLocalDepth();

                        Map<String, Bucket> oldDirectory = directory;

                        Map<String, Bucket> newDirectory = new HashMap<String, Bucket>();

                        double flagRatio = (Math.pow(2,globalDepth)-2) / 2;
                        boolean[] flag = new boolean[(int)flagRatio];

                        int f = 0;
                        for(f=0;f<flag.length;f++) {
                            flag[f] = true;
                        }

                        f=0;

                        for(int j=0;j < Math.pow(2,globalDepth); j++) {

                            String newHashSuffix = Integer.toBinaryString(j);
                            if (newHashSuffix.length() < globalDepth) {
                                int iter = globalDepth-newHashSuffix.length();
                                for(int h = iter ; h >0; h--) {
                                    newHashSuffix = "0" + newHashSuffix;
                                }
                            }
                           
                            Bucket bucket = null;
                            if(newHashSuffix.substring(1).equals(hashSuffix)) {
                                bucket = new Bucket(globalDepth ,bucketSize,true);
                            } else {

                                if(f < flag.length && flag[f]) {
                                    
                                    bucket = oldDirectory.get(newHashSuffix.substring(1));
                                    f++;
                                } else {
                                    bucket = new Bucket(oldDirectory.get(newHashSuffix.substring(1)).getLocalDepth() ,bucketSize, false);
                                }

                            }

                            newDirectory.put(newHashSuffix, bucket);
                        }

                        for(Map.Entry pointer : oldDirectory.entrySet()) {
                            Bucket current = ((Bucket)pointer.getValue());
                            if (current.getLocalDepth() == globalDepth) {
                                for(int r =0 ; r < current.getNumberOfElements() ; r++) {
                                    String currentStudentId = current.getRecords().get(r);
                                    String currentRecord = Integer.toBinaryString(Integer.parseInt(currentStudentId.substring(1)));
                                    if (currentRecord.length() <= globalDepth) {
                                        for(int x = globalDepth-currentRecord.length() ; x >0; x--) {
                                            currentRecord = "0" + currentRecord;
                                        }
                                    } else {
                                        currentRecord = currentRecord.substring(currentRecord.length()-globalDepth);
                                    }


                                    newDirectory.get(currentRecord).insert(currentStudentId);


                                }
                            }

                        }

                        oldDirectory.clear();

                        hashSuffix = binaryOfStudentId.substring(binaryOfStudentId.length()-globalDepth);
                        if (newDirectory.get(hashSuffix).getNumberOfElements() < bucketSize) {
                            newDirectory.get(hashSuffix).insert(studentID);
                            this.directory = newDirectory;
                            break;
                        } else {
                            this.directory = newDirectory;
                            continue;
                        }
                    }
                }

                i++;
            }
        }



    }




    public void leave(String studentID) {
        String key = search(studentID);
        for(int i=0; i< directory.get(key).getRecords().size();i++) {
            if (directory.get(key).getRecords().get(i).equals(studentID)) {
                directory.get(key).getRecords().remove(i);
                break;
            }
        }

        int level = globalDepth - directory.get(key).getLocalDepth();

        if(directory.get(key).getRecords().size() == 0) {
            char first;
            if (level == 0) {
                level++;
            }
            first = key.charAt(level-1);
            String splitImage;
            if(first == '0') {
                splitImage = "1" + key.substring(level);
            } else {
                splitImage = "0" + key.substring(level);
            }

            int iter = globalDepth-splitImage.length();
            for(int h=0; h<iter;h++ ) {
                splitImage = "0" + splitImage;
            }

            if (directory.get(splitImage).getLocalDepth() == directory.get(key).getLocalDepth()) {
                if (first == '0' && directory.get(splitImage).getRecords() != null) {
                    for(int i=0;i<directory.get(splitImage).getRecords().size();i++) {
                        directory.get(key).getRecords().add(directory.get(splitImage).getRecords().get(i));
                        directory.get(splitImage).getRecords().set(i,"!");
                    }

                    for(int k=0;k<directory.get(splitImage).getRecords().size();) {
                        if (directory.get(splitImage).getRecords().get(k).equals("!")) {
                            directory.get(splitImage).getRecords().remove(k);
                        } else {
                            k++;
                        }
                    }

                    directory.get(splitImage).setToNull();
                } else {
                    directory.get(key).setToNull();
                }

                if (directory.get(key).getLocalDepth() >1) {
                    directory.get(splitImage).decrementLocalDepth();
                    directory.get(key).decrementLocalDepth();
                }


                level++;

                if(directory.get(splitImage).getRecords() == null && directory.get(key).getRecords() == null && level < globalDepth) {


                    first = key.charAt(level-1);
                    String newSplitImage;
                    if(first == '0') {
                        newSplitImage= "1" + key.substring(level);
                    } else {
                        newSplitImage= "0" + key.substring(level);
                    }


                    iter = globalDepth-newSplitImage.length();


                    int iter2 = (int)Math.pow(2,iter);
                    for(int x =0; x<iter;x++) {
                        for(int y=0;y<iter2;y++) {
                            String deepSplitImage = Integer.toBinaryString(y) + newSplitImage;

                            if (directory.get(key).getLocalDepth() == (globalDepth - directory.get(deepSplitImage).getLocalDepth())) {
                                directory.get(deepSplitImage).decrementLocalDepth();
                            }
                        }

                        if ((x+1) < iter) {
                            directory.get(key).decrementLocalDepth();
                            directory.get(splitImage).decrementLocalDepth();
                        }

                    }

                }

            }

        }

        int count=0;
        for(Map.Entry elem : directory.entrySet()) {
            Bucket curr = (Bucket)elem.getValue();

            if(curr.getLocalDepth() < globalDepth) {
                count++;
            }
        }

        if(count == directory.size()) {
            globalDepth--;
            int iter = (int)Math.pow(2,globalDepth);

            Map<String, Bucket> newDirectory = new HashMap<String, Bucket>();

            for(int j=0;j<iter;j++) {
                String hs = Integer.toBinaryString(j);
                for(int k=hs.length();k<globalDepth;k++) {
                    hs = "0" + hs;
                }

                String newHs = "0"+hs;
                newDirectory.put(hs,directory.get(newHs));

            }

            directory.clear();

            directory = newDirectory;
        }


    }




    public String search(String studentID) {
        for (Map.Entry element : directory.entrySet()) {
            Bucket curr = ((Bucket)element.getValue());
            String currKey = ((String)element.getKey());
            if(curr.getRecords() != null) {
                for(int i=0; i< curr.getRecords().size(); i++) {
                    if (curr.getRecords().get(i).equals(studentID)) {
                        return currKey;
                    }
                }
            }
        }
        return "-1";
    }


    
    public void printLab() {
        

        System.out.println("Global depth : " + globalDepth);
        for(int i=0; i< Math.pow(2,globalDepth) ; i++) {
            String curr = Integer.toBinaryString(i);
            for(int j=curr.length(); j< globalDepth ; j++) {
                curr = "0" + curr;
            }

            System.out.print(curr + " : [Local depth:" + directory.get(curr).getLocalDepth() + "]");

            if (directory.get(curr).getRecords() != null) {
                for(int k=0; k< directory.get(curr).getRecords().size() ;k++) {
                    System.out.print("<" + directory.get(curr).getRecords().get(k) + ">");

                }
            } else {
                int x = globalDepth-directory.get(curr).getLocalDepth();
                String newCurr = curr.substring(curr.length()-directory.get(curr).getLocalDepth());
                for(int h=0; h<x;h++) {
                    newCurr = "0" + newCurr;
                }

                if(directory.get(newCurr).getRecords() != null) {
                    for(int k=0; k< directory.get(newCurr).getRecords().size() ;k++) {
                        System.out.print("<" + directory.get(newCurr).getRecords().get(k) + ">");

                    }
                }

            }

            System.out.print("\n");

        }
    }
}



class Bucket {

    private int localDepth;
    private int bucketSize;
    private ArrayList<String> studentIdRecords;

    public Bucket(int localDepth, int bucketSize, boolean hasBucket) {
        this.localDepth = localDepth;
        this.bucketSize = bucketSize;
        if (hasBucket) {
            studentIdRecords = new ArrayList<>(bucketSize);
        } else {
            studentIdRecords = null;
        }
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public void incrementLocalDepth() {
        localDepth = localDepth + 1;
    }

    public void decrementLocalDepth() {
        localDepth = localDepth - 1;
    }

    public ArrayList<String> getRecords() {
        return studentIdRecords;
    }

    public void setToNull() {
        studentIdRecords = null;
    }


    public int getNumberOfElements() {
        return studentIdRecords.size();
    }

    public void createBucket() {
        if (studentIdRecords == null) {
            studentIdRecords = new ArrayList<>(bucketSize);
        }
    }

    public void insert(String studentId) {
        if (studentIdRecords != null) {
            studentIdRecords.add(studentId);
        } else {
            studentIdRecords = new ArrayList<>(bucketSize);
            studentIdRecords.add(studentId);
        }

    }
}
