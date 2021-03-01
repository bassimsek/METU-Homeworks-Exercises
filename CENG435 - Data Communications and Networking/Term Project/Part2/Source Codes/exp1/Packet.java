//package com.orcunbassimsek;

import java.nio.ByteBuffer;

public class Packet {
    private static final int MAX_SIZE = 1000;
    private static final int HEADER_SIZE = 20;

    private int type;
    private int length;
    private int sequenceNumber;
    private long checkSum;
    private byte[] data;

    Packet(int type, int length, int sequenceNumber, long checkSum, byte[] data) {
        this.type = type;
        this.length = length;
        this.sequenceNumber = sequenceNumber;
        this.checkSum = checkSum;
        this.data = data;
    }


    public int getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getCheckSum() {
        return checkSum;
    }

    public byte[] getData() {
        return data;
    }

    // This method converts a Packet object to a byte array to prepare a packet for sending.
    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(type);
        buffer.putInt(length);
        buffer.putInt(sequenceNumber);
        buffer.putLong(checkSum);
        buffer.put(data, 0, length - HEADER_SIZE);
        return buffer.array();
    }

    /* This method extracts all information from received byte array,
     * and it creates real Packet object according to extracted informations.
     */
    public static Packet getPacket(byte[] bytes) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int type = buffer.getInt();
        int length = buffer.getInt();
        int sequenceNumber = buffer.getInt();
        long checkSum = buffer.getLong();
        if (length > HEADER_SIZE) {
            byte[] data = new byte[length - HEADER_SIZE];
            buffer.get(data, 0, length - HEADER_SIZE);
            return new Packet(type, length, sequenceNumber, checkSum, data);
        } else {
            return new Packet(type, length, sequenceNumber,0, new byte[0]);
        }
    }
}
