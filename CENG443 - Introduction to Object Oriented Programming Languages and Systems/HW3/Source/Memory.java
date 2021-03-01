//package com.orcunbassimsek;

public class Memory extends Part {

    private String socket;
    private String capacity;
    private String clockSpeed;


    public Memory(String type, String brand, String model, String socket, String capacity, String clockSpeed, String price) {
        super(type, brand, model, price);
        this.socket = socket;
        this.capacity = capacity;
        this.clockSpeed = clockSpeed;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getSocket() + "," + this.getCapacity() + "," + this.getClockSpeed() + "," + this.getPriceAsString();
        return result;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getClockSpeed() {
        return clockSpeed;
    }

    public void setClockSpeed(String clockSpeed) {
        this.clockSpeed = clockSpeed;
    }
}
