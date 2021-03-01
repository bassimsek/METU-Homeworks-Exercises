//package com.orcunbassimsek;

import java.io.IOException;
import java.net.*;
import java.util.*;


/* Node S sends messages to R3, and gets receiveTime information from node D through node R3 according to our shortest path. */
public class UDPNodeS {

    public static void main(String args[]) {
        String myNodeName = "S";
        String destinationNode = "R3";
        String destinationIP = "r3.61tp.ch-geni-net.instageni.cenic.net";
        int destinationPort = 1080;

		byte[] receiveData = new byte[2000];
        byte[] sendData = new byte[2000];

        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket(1050);
            ArrayList<Long> endToEndTimes = new ArrayList<Long>();
            long sendTimeFromNodeS, receiveTimeOnNodeD;

            System.out.println("Client " + myNodeName + " starts to send message to " + destinationNode + " server(Port: " + destinationPort + ").");

            for (int i = 0; i < 1000; i++) {

                InetAddress ipAddress = InetAddress.getByName(destinationIP);

                String discoveryMessage = "Any Random Message !!!";
                sendData = discoveryMessage.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, destinationPort);
                sendTimeFromNodeS = System.currentTimeMillis();
                clientSocket.send(sendPacket);

                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivedPacket);

                String receivedAsString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

                receiveTimeOnNodeD = Long.parseLong(receivedAsString);

				endToEndTimes.add(receiveTimeOnNodeD - sendTimeFromNodeS);
                System.out.println("For message " + (i + 1) + ": (from S to D among shortest path) - End-to-end: " + endToEndTimes.get(i) + " ms.");
	
	    }
	    
	    /* Averaging the end-to-end delays of 1000 messages that goes from node S to node D among shortest path. */ 
	    long sum = 0;
	    for (int i = 0; i < endToEndTimes.size(); i++) {
    	        sum += endToEndTimes.get(i);
	    }
	    double averageOfTimes = (double) sum / endToEndTimes.size();
	    System.out.println("Average : " +averageOfTimes + " ms.");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clientSocket.close();
        }


    }
}








