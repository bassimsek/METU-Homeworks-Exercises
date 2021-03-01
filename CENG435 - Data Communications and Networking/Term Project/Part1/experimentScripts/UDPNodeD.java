//package com.orcunbassimsek;

import java.io.IOException;
import java.net.*;

/* Node D just listens the messages that are coming from R3 according to shortest path */
public class UDPNodeD {

    public static void main(String args[]) {

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(1090);
            byte[] receiveData = new byte[2000];
            byte[] sendData = new byte[2000];
            

            int messageCount = 1;
        	System.out.println("Server on Node D starts to listen...");

        	/* It waits messages, and saved receiveTime of each message, 
        	then sends his information back to the sender which is R3 node in our case of course. */
            while (messageCount <= 1000) {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                Long receiveTime = System.currentTimeMillis();
                InetAddress sourceIpAddress = receivedPacket.getAddress();
                int port = receivedPacket.getPort();
                sendData = receiveTime.toString().getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sourceIpAddress, port);
                serverSocket.send(sendPacket);
                messageCount++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	serverSocket.close();
        }
    }

}


