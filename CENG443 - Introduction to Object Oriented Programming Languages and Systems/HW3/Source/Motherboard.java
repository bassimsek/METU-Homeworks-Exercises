//package com.orcunbassimsek;

public class Motherboard extends Part {

    private String socket;
    private String ramSlots;


    public Motherboard(String type, String brand, String model, String socket, String ramSlots, String price) {
        super(type, brand, model, price);
        this.socket = socket;
        this.ramSlots = ramSlots;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getSocket() + "," + this.getRamSlots() + "," + this.getPriceAsString();
        return result;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public String getRamSlots() {
        return ramSlots;
    }

    public void setRamSlots(String ramSlots) {
        this.ramSlots = ramSlots;
    }
}
