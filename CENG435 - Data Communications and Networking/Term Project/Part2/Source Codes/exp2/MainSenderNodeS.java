//package com.orcunbassimsek;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;


/* This time, sender node S contains two seperate threads for two paths(multi-homing)
 */
public class MainSenderNodeS {

    public static void main(String[] args) throws Exception {

        String router1 = "r1.61tp.ch-geni-net.instageni.cenic.net";
        String router2 = "r2.61tp.ch-geni-net.instageni.cenic.net";

        int myPort1 = 1115;
        int destinationPort1 = 1140;

        int myPort2 = 1145;
        int destinationPort2 = 1170;

        SenderSource senderOverR1 = new SenderSource(myPort1, router1, destinationPort1, "R1");
        Thread senderOverR1Thread = new Thread(senderOverR1);

        SenderSource senderOverR2 = new SenderSource(myPort2, router2, destinationPort2, "R2");
        Thread senderOverR2Thread = new Thread(senderOverR2);

        senderOverR1Thread.start();
        senderOverR2Thread.start();
    }
}



/* In this class, I have 2 threads for each path, thus in total I have 4 threads to handle multi-homing */
class SenderSource implements Runnable {

    private static volatile int base = 0;
    private static volatile int nextSequenceNumber = 0;
    private volatile boolean isFileSendCompleted = false;
    private volatile boolean isLinkDown = false; // Link-down flag.

    private static final int HEADER_SIZE = 20;
    private static final int PAYLOAD_SIZE = 980;
    private static final int MAX_SEQ_NO = 256;
    private static final Semaphore windowSize = new Semaphore(20); // I increased window size to 20 for this part

    private static FileInputStream fileStream;

    static {
        try {
            fileStream = new FileInputStream("input2.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    ;
    private DatagramSocket socket;
    private InetAddress receiverAddress;
    private int port;
    private String destinationNodeName;

    private static Deque<Packet> queue = new ArrayDeque<>();
    private static final Object queueLock = new Object();

    private static Timer timerForRetransmission;
    private static long timeoutForRetransmission = 1000;
    private static final Object timerLock = new Object();

    private static final Object fileLock = new Object();

    /* There are two sending payload Thread for two paths, and there are two receiving ACK Thread for two paths.
     * Therefore, I also needed that updating of nextSequenceNumber field must be thread-safe. Thus, I need this seqLock object for this part.
     */
    private static final Object seqLock = new Object();


    SenderSource(int myPort, String hostname, int port, String destinationNodeName) throws Exception {
        socket = new DatagramSocket(myPort);
        receiverAddress = InetAddress.getByName(hostname);
        this.port = port;
        this.destinationNodeName = destinationNodeName;
        //queue = new ArrayDeque<>();
    }



    private void receiveACKs() {
        byte[] buffer = new byte[HEADER_SIZE];
        DatagramPacket receivedACK = new DatagramPacket(buffer, buffer.length);
        Packet packet;
        boolean isFirstPacketReceived = true;

        try {
            socket.setReceiveBufferSize(0xFFFF);

            while (!isFileSendCompleted || !queue.isEmpty()) {

                socket.receive(receivedACK);

                if (isFirstPacketReceived) {
                    socket.setSoTimeout(10000);
                    isFirstPacketReceived = false;
                }

                packet = Packet.getPacket(receivedACK.getData());
                System.out.println("Received packet -- Length: "+ packet.getLength() + " -- ACK No: " + packet.getSequenceNumber());
                int ACKNo = packet.getSequenceNumber();

                int receiveNo = ACKNo - base + 1;
                if (ACKNo < base) {
                    receiveNo += MAX_SEQ_NO;
                }

                if (receiveNo <= 20) {
                    synchronized (queueLock) {
                        for (int i = 0; i < receiveNo; i++) {
                            queue.poll();
                            windowSize.release();
                        }
                    }
                    base = (ACKNo + 1) % MAX_SEQ_NO;
                }

                if (base == nextSequenceNumber) {
                    timerForRetransmission.cancel();
                } else {
                    restartTimer();
                }

            }
        } catch(SocketTimeoutException se) {
            /* Link-failure detection, if node S can not receive any packet for a specific path (for example S-R2-D) in 10 seconds,
             * it terminates itself by deciding a link-down. */
            System.out.println("****** Link for S - " + destinationNodeName + " is down. File transmission over this path is terminated. ******");
            isLinkDown = true;
            socket.close();
        } catch (Exception e) {
            System.out.println("Exception(receiving ACK):");
            //e.printStackTrace();
        }
    }



    private Thread createReceiveACKsThread() {
        Thread receiveACKsThread = new Thread(this::receiveACKs);
        receiveACKsThread.start();

        return receiveACKsThread;
    }


    @Override
    public void run() {

        try {

            timerForRetransmission = new Timer();

            Thread receiveACKsThread = createReceiveACKsThread();

            CheckSum checkSumObj = new CheckSum();

            System.out.println("Start sending file.");
            while (true) {

                byte[] buffer = new byte[PAYLOAD_SIZE];
                int payloadSize;

                if(isLinkDown == true) {
                    break;
                }

                synchronized (fileLock) {
                    payloadSize = fileStream.read(buffer, 0, PAYLOAD_SIZE);
                }

                if (payloadSize < 0) {
                    isFileSendCompleted = true;
                    break;
                }

                String checkSum = checkSumObj.makeInvertedChecksum(buffer);
                Packet packet = new Packet(0,payloadSize + HEADER_SIZE, nextSequenceNumber, Long.parseLong(checkSum), buffer);

                windowSize.acquire();
                synchronized (queueLock) {
                    queue.offer(packet);
                }

                DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getBytes().length, receiverAddress, port);
                socket.send(sendPacket);
                System.out.println("Packet sent, Length: " + packet.getLength() + " -- SEQ No: " + packet.getSequenceNumber());

                if (base == nextSequenceNumber) {
                    restartTimer();
                }

                synchronized(seqLock) {
                	nextSequenceNumber = (nextSequenceNumber + 1) % MAX_SEQ_NO;
                }
                
            }

            if (isLinkDown) {
                fileStream.close();
                timerForRetransmission.cancel();
            } else {

                receiveACKsThread.join();

                endSenderSession();

                System.out.println("Finish sending file from source node S...");
                socket.close();
            }
        } catch(Exception e) {
            //e.printStackTrace();
        }

    }


    private void endSenderSession()  {
        try {
            Packet packet1 = new Packet(2, HEADER_SIZE, base, 0 ,new byte[0]);
            DatagramPacket sendPacket = new DatagramPacket(packet1.getBytes(), packet1.getBytes().length, receiverAddress, port);
            socket.send(sendPacket);
            System.out.println("Packet sent -- type:EOF -- SEQ No: "+ base);
            // wait for EOF
            while (true) {
                byte[] buffer = new byte[HEADER_SIZE];
                DatagramPacket receiveDatagram = new DatagramPacket(buffer, buffer.length);
                socket.receive(receiveDatagram);
                Packet packet2 = Packet.getPacket(receiveDatagram.getData());
                if (packet2.getType() == 2) {
                    System.out.println("Packet received -- type:EOF -- SEQ No: " + packet2.getSequenceNumber());
                    break;
                } else if (packet2.getType() == 1){
                    System.out.println("Packet received -- type:ACK -- SEQ No: " + packet2.getSequenceNumber());
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

    }


    class TimeoutTask extends TimerTask {
        public void run() {

            synchronized (queueLock) {
                Iterator<Packet> iterator = queue.iterator();
                while (iterator.hasNext()) {
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(iterator.next().getBytes(), iterator.next().getBytes().length, receiverAddress, port);
                        socket.send(sendPacket);
                        System.out.println("Packet sent, Length: " + iterator.next().getLength() + " -- SEQ No: " + iterator.next().getSequenceNumber());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            synchronized (timerLock) {
                timerForRetransmission.schedule(new TimeoutTask(), timeoutForRetransmission);
            }
        }
    }


    private void restartTimer() {
        synchronized (timerLock) {
            timerForRetransmission.cancel();
            timerForRetransmission = new Timer();
            timerForRetransmission.schedule(new TimeoutTask(), timeoutForRetransmission);
        }
    }
}
