//package com.orcunbassimsek;

import java.io.IOException;
import java.net.*;


/*Node R3 gets message from node S and deliver it to node D as a bridge. */
public class UDPNodeR3 {

    public static void main(String args[]) {

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(1080);
            byte[] receiveData = new byte[2000];
            byte[] sendData = new byte[2000];
            InetAddress destinationIpAddress;
            int port;
            

        	int messageCount = 1;
            System.out.println("Server on Node R3 starts to listen...");

            /* Node R3 firstly listens to get message from initial node S.
               Then, it delivers this message to node D.
               Then, it again starts to listen to get receiveTime information from node D.
               Then, it sends this receiveTime information to S node finally. */
            while (messageCount <= 1000) {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
  				destinationIpAddress = InetAddress.getByName("d.61tp.ch-geni-net.instageni.cenic.net");
	
                port = 1090;
                sendData = receivedPacket.getData();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destinationIpAddress, port);
                serverSocket.send(sendPacket);

                receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                destinationIpAddress = InetAddress.getByName("s.61tp.ch-geni-net.instageni.cenic.net");
                port = 1050;
		

				String receivedAsString = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                sendData = receivedAsString.getBytes();

                sendPacket = new DatagramPacket(sendData, sendData.length, destinationIpAddress, port);
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
