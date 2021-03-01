//package com.orcunbassimsek;

/* Utility class for using checksum related operations for both sender node S and receiver node D */
public class CheckSum {

    /* It calculates the inverted checksum before put it in header of packets.
     * This method is used by sender node S.
     */
    public String makeInvertedChecksum(byte[] data) {
        
        String sum = calculateChecksum(data);
        String checkSum = "";

        for(int i=0; i<sum.length(); i++){
            if(sum.charAt(i)=='0') {
                checkSum += "1";
            }
            else {
                checkSum += "0";
            }
        }

        return checkSum;
    }


    /* It calculates the checksum for given payload data as a byte array.
     * This method is used by receiver node D for received packet's payload data,
     * and for check if it equals the received packet's header's checksum field or not.
     */ 
    public String calculateChecksum(byte[] data) {

        String checkSum = "0000000000000000";  // 16 bits
        for(int i=0; i<data.length; i++){
            String bits = decimalToBinary(data[i]);
            checkSum = binaryAddition(checkSum,bits);
        }

        return checkSum;
    }



    /* It converts decimal integer to a binary integer and,
     * returns this binary integer(16 bit) as a string.
     */ 
    public  String decimalToBinary(int number){
        
        // parameter number is between 0-255
        StringBuilder binary = new StringBuilder();

        while(number > 1){
            int remain = number % 2;
            binary.insert(0, remain + "");
            number = number / 2;
        }

        binary.insert(0, number + "");

        // 1 byte = 8 bits, so left bits is filled with 0
        int remainingBits = 8 - binary.length();
        StringBuilder remaining = new StringBuilder();
        for(int i=0;i<remainingBits;i++) {
            remaining.insert(0, "0");

        }

        binary.insert(0,remaining);
        binary.insert(0, "00000000"); // Adding extra 8 bits to make it 16 bits

        return binary.toString();
    }


    /* It calculates the sum of two binary numbers given as Strings */
    public  String binaryAddition(String s1, String s2){
        
        int carry = 0;
        int firstInt, secondInt;

        int check = s2.length();

        for(int j=0; j<s1.length()-check ;j++) {
            s2 = "0" + s2;
        }

        StringBuilder sum = new StringBuilder(17);

        for (int i = s1.length()-1; i >= 0; i--) {
            int sumInt = 0;
            firstInt = Character.getNumericValue(s1.charAt(i));
            secondInt = Character.getNumericValue(s2.charAt(i));

            sumInt = firstInt + secondInt + carry;
            sum.insert(0, (sumInt%2) + "");
            carry = sumInt/2;
        }

        if(carry == 1)
            sum.insert(0, carry + "");
        if(sum.length() > 16){
            return binaryAddition("000000000000000" + sum.substring(0, 1) , sum.substring(1, sum.length()) );
        }

        return sum.toString();
    }
}
