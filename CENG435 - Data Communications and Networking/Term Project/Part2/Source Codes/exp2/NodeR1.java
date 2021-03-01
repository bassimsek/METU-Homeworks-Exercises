//package com.orcunbassimsek;

import java.net.*;


// It is the class that contains runner main method for router node R1
public class NodeR1 {

    public static void main(String args[]) {

        Router UDPClientForNodeD = new Router(1140, "D", "d.61tp.ch-geni-net.instageni.cenic.net", 1120);
        Thread UDPClientThreadForNodeD = new Thread(UDPClientForNodeD);

        Router UDPClientForNodeS = new Router(1130, "S", "s.61tp.ch-geni-net.instageni.cenic.net", 1115);
        Thread UDPClientThreadForNodeS = new Thread(UDPClientForNodeS);

        UDPClientThreadForNodeD.start();
        UDPClientThreadForNodeS.start();

    }
}



/* It is the real job class that implements correct routing for second part of the project,
 * for router node R1.
 */
class Router implements Runnable {

    public int myPort;
    public String destinationNode;
    public String destinationIP;
    public int destinationPort;
    public final int HEADER_SIZE = 20;


    public Router(int myPort, String destinationNode, String destinationIP, int destinationPort) {
        this.myPort = myPort;
        this.destinationNode = destinationNode;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
    }


    /* It implements the correct routing for the case of second part of the project */
    @Override
    public void run() {

        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket(myPort);
            InetAddress ipAddress = InetAddress.getByName(destinationIP);
            clientSocket.setReceiveBufferSize(0xFFFF);
            boolean isFirstPacketReceived = true;

            while(true) {

                byte[] receiveData = new byte[1000];
                byte[] sendData = new byte[1000];
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivedPacket);

                Packet received = Packet.getPacket(receivedPacket.getData());
                System.out.println("Received packet -- Length: "+ received.getLength() + " -- SEQ No: " + received.getSequenceNumber());

                if (isFirstPacketReceived) {
                    clientSocket.setSoTimeout(10000);
                    isFirstPacketReceived = false;
                }


                if(received.getType() == 2) {

                    System.out.println("Packet received -- EOF, Length: " + received.getLength() + " -- SEQ No: " + received.getSequenceNumber());
                    Packet packet = new Packet(2, HEADER_SIZE, received.getSequenceNumber(), 0 ,new byte[0]);
                    DatagramPacket sendPacket2 = new DatagramPacket(packet.getBytes(), packet.getBytes().length, ipAddress, destinationPort);
                    clientSocket.send(sendPacket2);
                    System.out.println("Packet sent -- EOF -- SEQ No: "+ received.getSequenceNumber());
                    break;
                }

                sendData = receivedPacket.getData();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, destinationPort);
                clientSocket.send(sendPacket);
                System.out.println("Packet sent, Length: " + received.getLength() + " -- SEQ No: " + received.getSequenceNumber() + " -- Destination: " + destinationNode + " node.");
            }

        } catch(Exception e) {
            //e.printStackTrace();
        } finally {
            clientSocket.close();
        }

    }
}








