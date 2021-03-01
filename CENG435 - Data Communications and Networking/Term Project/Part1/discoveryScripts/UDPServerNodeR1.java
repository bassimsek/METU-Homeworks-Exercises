//package com.orcunbassimsek;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;


/* Main class which executes main Server thread for Node R1.*/
public class UDPServerNodeR1 {

    public static void main(String args[]) {

        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(1060);
            byte[] receiveData = new byte[2000];
            byte[] sendData = new byte[2000];
            boolean isMessagingStarted = false;

            int messageCount = 1;
            System.out.println("Server on Node R1 starts to listen...");

        	/* It expects total 3000 messages from negihbour S(1000 messages)-R2(1000 messages)-D(1000 messages) clients. 
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

                /* Node R1 starts its sender Clients for each of its neighbour node's servers. */
                if (isMessagingStarted == false) {
                    Client UDPClientForNodeS = new Client("R1","S","s.61tp.ch-geni-net.instageni.cenic.net",1050);
		            Thread UDPClientThreadForNodeS = new Thread(UDPClientForNodeS);
		            
		            Client UDPClientForNodeR2 = new Client("R1","R2","r2.61tp.ch-geni-net.instageni.cenic.net",1070);
		            Thread UDPClientThreadForNodeR2 = new Thread(UDPClientForNodeR2);

		            Client UDPClientForNodeD =new Client("R1","D","d.61tp.ch-geni-net.instageni.cenic.net",1090);
		            Thread UDPClientThreadForNodeD = new Thread(UDPClientForNodeD);

		            UDPClientThreadForNodeS.start();
		            UDPClientThreadForNodeR2.start();
		            UDPClientThreadForNodeD.start();

                    isMessagingStarted = true;
                }
                messageCount++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
        	serverSocket.close();
        }
    }

}


/* Sender Client thread implementation for Node R1 */
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

            /* Averaging RTT values and creating link_cost(X-Y).txt files. */
        	long sum = 0;
            for (int i = 0; i < RTTtimes.size(); i++) {
                sum += RTTtimes.get(i);
            }
            double averageOfTimes = (double) sum / RTTtimes.size();
            System.out.println("For link (" +myNodeName+ "-" +destinationNode+ ") , Average : " +averageOfTimes);

            PrintWriter writer = new PrintWriter("link_cost("+myNodeName+"-"+destinationNode+").txt", "UTF-8");
            writer.println("Average of RTT's for ("+myNodeName+"-"+destinationNode+") link: " +averageOfTimes+ " ms.");
            writer.close();

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
        	clientSocket.close();
        }

    }
}






