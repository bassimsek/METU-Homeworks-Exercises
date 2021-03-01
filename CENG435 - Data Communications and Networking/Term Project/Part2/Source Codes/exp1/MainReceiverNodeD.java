//package com.orcunbassimsek;

import java.io.*;
import java.net.*;

// It is the class that contains runner main method for receiver node D
public class MainReceiverNodeD {


    public static void main(String[] args) throws Exception {

        String fileName = "output1.txt";

        int port = 1090;
        DatagramSocket socket = new DatagramSocket(port);

        // node D sends ACK responses to S node over R3 node. Thus, it needs to hostname of R3 node.
        ReceiverDestination receiver = new ReceiverDestination(socket, fileName, "r3.61tp.ch-geni-net.instageni.cenic.net", 1070);
        receiver.start();
    }

}


/* It is the real job class that includes all important features of Reliable Data Protocol(RDT)
 * for receiver node S.
 */
class ReceiverDestination {

    private static final int HEADER_SIZE = 20; // header size = 20 bytes
    private static final int BUFFER_SIZE = 1000; // packet size, in total
    private static final int MAX_SEQ_NO = 256; // Sequence number range

    private int expectedSequenceNumber = 0; // expected Sequence Number for true ordering of received packets.
    private boolean packetCorrupted = false;

    private DatagramSocket socket;
    private FileOutputStream fout; // to write correctly received packets' payload to our "output.txt" file.
    private CheckSum check; // to use checksum related methods of CheckSum class

    private InetAddress receiverAddress;
    private int receiverPort;

    /* Constructor */
    ReceiverDestination(DatagramSocket socket, String file, String receiverHostname, int receiverPort) throws Exception {
        this.socket = socket;
        fout = new FileOutputStream(file);
        this.receiverAddress = InetAddress.getByName(receiverHostname);
        this.receiverPort = receiverPort;
        check = new CheckSum();
    }


    /* It implements the algorithm for receiving payload packets and sending ACK responses correctly */
    public void start() throws Exception {

        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket receiveDatagram = new DatagramPacket(buffer, buffer.length);
        socket.setReceiveBufferSize(0xFFFF);
        boolean isFirstPacketReceived = true;

        System.out.println("Start to receive data for node D...");
        while(true) {
            socket.receive(receiveDatagram);

            /* after taking first packet, set timeout for socket's receive operation's blocking time.
               For termination purposes. */
            if (isFirstPacketReceived) {
                socket.setSoTimeout(10000);
                isFirstPacketReceived = false;
            }

            // get packet
            Packet packet = Packet.getPacket(receiveDatagram.getData());

            /* if it gets type 2 packet(end-of-file packet), it terminates itself properly.
             * else if it gets type 0 packet(payload packet), it firslty checks the "Checksum" field of received packet.
             * If sum of this field and calculated checksum from received packet's payload does not contain any "0" bit, then there is no corruption in packet's payload.
             * After that, also if received packet's sequence number is equal to expected sequence number(i.e. in true order), it writes the
             * received packet's payload data to my "output.txt" file, and increase the expected sequence number by 1.
             * Else, it sends lastly successfully received packet's ACK response again to the sender.
             */ 
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
                    fout.write(packet.getData());
                    sendACK(expectedSequenceNumber);
                    expectedSequenceNumber = (expectedSequenceNumber + 1) % MAX_SEQ_NO;
                } else {
                    sendACK(((expectedSequenceNumber + MAX_SEQ_NO - 1) % MAX_SEQ_NO));
                }
            }

            packetCorrupted = false;
        }

        // close socket and file outputstream
        System.out.println("Finish receiving file at Node D...");
        fout.close();
        socket.close();
    }

    // Helper method for sending ACK type packet (type 1).
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

