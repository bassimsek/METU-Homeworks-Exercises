//package com.orcunbassimsek;

import java.io.*;
import java.net.*;


/* For this second part, receiver node D runs two threads for receive messages from two paths(multi-homing). */
public class MainReceiverNodeD {


    public static void main(String[] args) throws Exception {

        String router1 = "r1.61tp.ch-geni-net.instageni.cenic.net";
        String router2 = "r2.61tp.ch-geni-net.instageni.cenic.net";

        int myPort1 = 1120;
        int destinationPort1 = 1130;

        int myPort2 = 1150;
        int destinationPort2 = 1160;

        ReceiverDestination senderOverR1 = new ReceiverDestination(myPort1, router1, destinationPort1, "R1");
        Thread senderOverR1Thread = new Thread(senderOverR1);

        ReceiverDestination senderOverR2 = new ReceiverDestination(myPort2, router2, destinationPort2, "R2");
        Thread senderOverR2Thread = new Thread(senderOverR2);

        senderOverR1Thread.start();
        senderOverR2Thread.start();
    }

}


class ReceiverDestination implements Runnable {

    private static final int HEADER_SIZE = 20;
    private static final int BUFFER_SIZE = 1000;
    private static final int MAX_SEQ_NO = 256;

    private static volatile int expectedSequenceNumber = 0;
    private boolean packetCorrupted = false;

    private DatagramSocket socket;
    private static FileOutputStream fout;

    static {
        try {
            fout = new FileOutputStream("output2.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Object fileLock = new Object();
    private CheckSum check;

    private InetAddress receiverAddress;
    private int receiverPort;
    private String destinationNodeName;


    ReceiverDestination(int myPort, String receiverHostname, int receiverPort, String destinationNodeName) throws Exception {
        this.socket = new DatagramSocket(myPort);
        this.receiverAddress = InetAddress.getByName(receiverHostname);
        this.receiverPort = receiverPort;
        this.destinationNodeName = destinationNodeName;
        check = new CheckSum();
    }

    @Override
    public void run() {

        try {

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket receiveDatagram = new DatagramPacket(buffer, buffer.length);
            socket.setReceiveBufferSize(0xFFFF);
            boolean isFirstPacketReceived = true;

            System.out.println("Start to receive data for node D...");
            while(true) {
                socket.receive(receiveDatagram);

                if (isFirstPacketReceived) {
                    socket.setSoTimeout(10000);
                    isFirstPacketReceived = false;
                }

                Packet packet = Packet.getPacket(receiveDatagram.getData());

                if (packet.getType() == 2) {
                    System.out.println("Packet received -- EOF, Length: " + packet.getLength() + " -- SEQ No: " + packet.getSequenceNumber());
                    Packet packet2 = new Packet(2, HEADER_SIZE, packet.getSequenceNumber(), 0 ,new byte[0]);
                    DatagramPacket sendPacket2 = new DatagramPacket(packet2.getBytes(), packet2.getBytes().length, receiverAddress, receiverPort);
                    socket.send(sendPacket2);
                    System.out.println("Packet sent -- EOF -- SEQ No: "+ packet.getSequenceNumber());

                    break;

                } else if (packet.getType() == 0){
                    System.out.println("Packet received, Length: " + packet.getLength() + " -- SEQ No: " + packet.getSequenceNumber());
                    String receivedChecksum = packet.getCheckSum() +"";
                    String calculatedChecksum = check.calculateChecksum(packet.getData());
                    String sumOfChecksums = check.binaryAddition(calculatedChecksum, receivedChecksum);

                    for(int i=0;i<sumOfChecksums.length();i++) {
                        if(sumOfChecksums.charAt(i) == '0') {
                            packetCorrupted = true;
                            break;
                        }
                    }

                    if (packet.getSequenceNumber() == expectedSequenceNumber && packetCorrupted == false) {
                        synchronized (fileLock) {
                            fout.write(packet.getData());
                            sendACK(expectedSequenceNumber);
                        }
                        expectedSequenceNumber = (expectedSequenceNumber + 1) % MAX_SEQ_NO;
                    } else {
                        sendACK(((expectedSequenceNumber + MAX_SEQ_NO - 1) % MAX_SEQ_NO));
                    }
                }

                packetCorrupted = false;
            }

            System.out.println("Finish receiving file at Node D...");
            fout.close();
            socket.close();

        } catch (Exception e) {
            //e.printStackTrace();
        }

    }


    private void sendACK(int ackNo) {
        try {
            Packet packet2 = new Packet(1, HEADER_SIZE, ackNo, 0, new byte[0]);
            DatagramPacket sendPacket = new DatagramPacket(packet2.getBytes(), packet2.getBytes().length, receiverAddress, receiverPort);
            socket.send(sendPacket);
            System.out.println("Packet Sent  -- ACK No: " + ackNo);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

