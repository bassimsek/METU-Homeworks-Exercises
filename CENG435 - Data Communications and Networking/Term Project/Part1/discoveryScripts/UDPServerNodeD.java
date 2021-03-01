//package com.orcunbassimsek;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;


/* Main class which executes main Server thread for Node D.*/
public class UDPServerNodeD {

    public static void main(String args[]) {

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(1090);
            byte[] receiveData = new byte[2000];
            byte[] sendData = new byte[2000];
            boolean isMessagingStarted = false;

            int messageCount = 1;
        	System.out.println("Server on Node D starts to listen...");

        	/* It expects total 3000 messages from negihbour R1(1000 messages)-R2(1000 messages)-R3(1000 messages) clients. 
        	Receive method of DatagramSocket is blocking.
        	Send method is just sending "ACK" message to sender client back. */
            while (messageCount <= 3000) {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                InetAddress sourceIpAddress = receivedPacket.getAddress();
                int port = receivedPacket.getPort();
                sendData = "ACK".getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sourceIpAddress, port);
                serverSocket.send(sendPacket);

                /* Node D starts its sender Clients for each of its neighbour node's servers. */
                if (isMessagingStarted == false) {
                    Client UDPClientForNodeR1 = new Client("D","R1","r1.61tp.ch-geni-net.instageni.cenic.net",1060);
		            Thread UDPClientThreadForNodeR1 = new Thread(UDPClientForNodeR1);

		            Client UDPClientForNodeR2 = new Client("D","R2","r2.61tp.ch-geni-net.instageni.cenic.net",1070);
		            Thread UDPClientThreadForNodeR2 = new Thread(UDPClientForNodeR2);

		            Client UDPClientForNodeR3 = new Client("D","R3","r3.61tp.ch-geni-net.instageni.cenic.net",1080);
		            Thread UDPClientThreadForNodeR3 = new Thread(UDPClientForNodeR3);

		            UDPClientThreadForNodeR1.start();
		            UDPClientThreadForNodeR2.start();
		            UDPClientThreadForNodeR3.start();

                    isMessagingStarted = true;
                }
                messageCount++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	serverSocket.close();
        }
    }

}



/* Sender Client thread implementation for Node D */
class Client implements Runnable {

    public String myNodeName;
    public String destinationNode;
	public String destinationIP;
    public int destinationPort;

    /* Destination informations are collected in this constructor */
    public Client(String myNodeName, String destinationNode, String destinationIP, int destinationPort) {
        this.myNodeName = myNodeName;
        this.destinationNode = destinationNode;
        this.destinationIP = destinationIP;
        this.destinationPort = destinationPort;
    }

    /* In this run() method, Client sends 1000 discovery message to specific neighbour node's server.
    And waits the "ACK" with clientSocket.receive() blocking method.
    Then, it did RTT calculations as explained in our report. */
    @Override
    public void run() {

    	DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
            ArrayList<Long> RTTtimes = new ArrayList<Long>();
            long sendTime, receiveTime;

            System.out.println("Client " +myNodeName+ " starts to send message to " +destinationNode+ " server(Port: " +destinationPort+ ").");

            for (int i = 0; i < 1000; i++) {

                InetAddress ipAddress = InetAddress.getByName(destinationIP);
                byte[] receiveData = new byte[2000];
                byte[] sendData = new byte[2000];
                String discoveryMessage = "Any random message !!";
                sendData = discoveryMessage.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, destinationPort);
                sendTime = System.currentTimeMillis();
                clientSocket.send(sendPacket);

                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivedPacket);
                receiveTime = System.currentTimeMillis();

                RTTtimes.add(receiveTime - sendTime);
                System.out.println("For message " +(i+1)+ ": (" +myNodeName+ " as Sender - " +destinationNode+ " as Receiver) RTT: " +RTTtimes.get(i));
            }

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
        	clientSocket.close();
        }

    }
}

