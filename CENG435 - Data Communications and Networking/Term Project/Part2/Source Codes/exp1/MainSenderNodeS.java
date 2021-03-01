//package com.orcunbassimsek;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Semaphore;

// It is the class that contains runner main method for sender node S
public class MainSenderNodeS {

    public static void main(String[] args) throws Exception {

        String fileName = "input1.txt";
        String hostName = "r3.61tp.ch-geni-net.instageni.cenic.net"; // node S only communicate with R3 node directly.
        int destinationPort = 1080;

        SenderSource sender = new SenderSource(fileName, hostName, destinationPort);
        sender.start();
    }
}



/* It is the real job class that includes all important features of Reliable Data Protocol(RDT)
 * for sender node S.
 */
class SenderSource {

    private volatile int base = 0; // base number of windows - it is thread-safe.
    private volatile int nextSequenceNumber = 0; // sequence Number - it is thread-safe.
    private volatile boolean isFileSendCompleted = false; // flag for termination process of this node.

    private static final int HEADER_SIZE = 20; // header size = 20 bytes
    private static final int PAYLOAD_SIZE = 980; // payload size = 980 bytes, in total, one packet size = 1000 bytes
    private static final int MAX_SEQ_NO = 256; // Sequence number range
    private static final Semaphore windowSize = new Semaphore(10); // window size

    private FileInputStream fileStream; // pointer for reading data from "input.txt" file
    private DatagramSocket socket;
    private InetAddress receiverAddress; //i.e. R3 address
    private int port; // receiver port, i.e. R3 port

    private Deque<Packet> queue; // sender buffer
    private static final Object queueLock = new Object(); // to make adding or deleting a packet from sender buffer safely

    private Timer timer; // timer for retransmission of all unACKed packets.
    private long timeout = 1000; // timeout for retransmission. It is now 1000 msec = 1 sec, but it can be changed to any value.
    private static final Object timerLock = new Object(); // to make it timer related operations thread-safe.


    /* Constructor */
    SenderSource(String file, String hostname, int port) throws Exception {
        fileStream = new FileInputStream(file);
        socket = new DatagramSocket(1060);
        receiverAddress = InetAddress.getByName(hostname);
        this.port = port;
        queue = new ArrayDeque<>();
    }



    /* Receiving ACK response packets from the node D over R3,
     * and algorithm of this process is implemented in this method.
     */ 
    private void receiveACKs() {
        byte[] buffer = new byte[HEADER_SIZE];
        DatagramPacket receivedACK = new DatagramPacket(buffer, buffer.length);
        Packet packet;

        try {
            socket.setReceiveBufferSize(0xFFFF); // increase the receive buffer size of socket to ensure handle waiting packets without any overflow.
            boolean isFirstPacketReceived = true;

            while (!isFileSendCompleted || !queue.isEmpty()) {
                
                socket.receive(receivedACK);
                /* after taking first packet, set timeout for socket's receive operation's blocking time.
                   For termination purposes. */
                if (isFirstPacketReceived) {
                    socket.setSoTimeout(10000);
                    isFirstPacketReceived = false;
                }

                packet = Packet.getPacket(receivedACK.getData());
                System.out.println("Received packet -- Length: " + packet.getLength() + " -- ACK No: " + packet.getSequenceNumber());

                // get ACK number of received packet
                int ACKNo = packet.getSequenceNumber();

                // calculate the number of received packets
                int receiveNo = ACKNo - base + 1;
                if (ACKNo < base) {
                    receiveNo += MAX_SEQ_NO;
                }

                // delete the received packets from buffer
                if (receiveNo <= 10) {
                    synchronized (queueLock) {
                        for (int i = 0; i < receiveNo; i++) {
                            queue.poll();
                            windowSize.release();
                        }
                    }
                    // update base number
                    base = (ACKNo + 1) % MAX_SEQ_NO;
                }

                // reset timer
                if (base == nextSequenceNumber) {
                    timer.cancel();
                } else {
                    restartTimer();
                }

            }
        } catch (Exception e) {
            System.out.println("Exception(receiving ACK):");
            e.printStackTrace();
        }
    }


    // Helper method for creating receive ACK thread which run above receiveACKs() method in its run() method.
    private Thread createReceiveACKsThread() {
        Thread receiveACKsThread = new Thread(this::receiveACKs);
        receiveACKsThread.start();

        return receiveACKsThread;
    }


    /* This method implements the algorithm for sending a payload (taken from "input.txt") carried packets
     * to receiver node D over node R3.
     */
    public void start() throws Exception {

        // create a timer
        timer = new Timer();

        // create new thread to receive ACK packets
        Thread receiveACKsThread = createReceiveACKsThread();

        // create checksum object to calculate inverted cheksum of payload taken from "input.txt"
        CheckSum checkSumObj = new CheckSum();

        System.out.println("Start sending file.");

        while (true) {
            byte[] buffer = new byte[PAYLOAD_SIZE];

            // read data from "input.txt, fileStream.read() method automatically increase the file pointer by PAYLOAD_SIZE(980 bytes)"
            int payloadSize = fileStream.read(buffer, 0, PAYLOAD_SIZE);
            // if returned payloadSize value from fileStream.read() method is equal to 0, file pointer is at the end-of-file
            if (payloadSize < 0) {
                isFileSendCompleted = true;
                break;
            }

            // calculate inverted checksum from currently taken payload from "input.txt"
            String checkSum = checkSumObj.makeInvertedChecksum(buffer);
            Packet packet = new Packet(0,payloadSize + HEADER_SIZE, nextSequenceNumber, Long.parseLong(checkSum), buffer);

            // puts packet in a window as unACKed packet by acquiring a permit from a Semaphore(windowSize)
            windowSize.acquire();
            synchronized (queueLock) {
                queue.offer(packet);
            }

            // and then send the packet
            DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getBytes().length, receiverAddress, port);
            socket.send(sendPacket);
            System.out.println("Packet sent, Length: " + packet.getLength() + " -- SEQ No: " + packet.getSequenceNumber());

            // reset timer
            if (base == nextSequenceNumber) {
                restartTimer();
            }

            // update next sequence number field accordingly
            nextSequenceNumber = (nextSequenceNumber + 1) % MAX_SEQ_NO;
        }

        // wait receiveACKs thread before continue
        receiveACKsThread.join();

        // termination of node S
        endSenderSession();

        System.out.println("Finish sending file from source node S...");
        socket.close();
        fileStream.close();
        timer.cancel();
    }


    /* This method implements the termination mechanism of sender node S.
     * It sends header-sized type 2 packet(it is end-of-file packet).
     * And then, it continues to receive remaining ACKs, and at the end it receives type 2 packet and terminates itself.
     */ 
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
            e.printStackTrace();
        }

    }


    /* Inner class to implement run() method of TimerTask class to implement a job which is
     * retransmission of all unACKed packets when timeout(1 second) past.
     */ 
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
                        //e.printStackTrace();
                    }
                }
            }

            // schedule a new timeout task
            synchronized (timerLock) {
                timer.schedule(new TimeoutTask(), timeout);
            }
        }
    }


    /* restarts the timer */
    private void restartTimer() {
        synchronized (timerLock) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimeoutTask(), timeout);
        }
    }
}
