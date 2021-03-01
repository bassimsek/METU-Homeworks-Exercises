//package com.orcunbassimsek;

public class CPU extends Part {

    private String coreCount;
    private String clockSpeed;


    public CPU(String type, String brand, String model, String coreCount, String clockSpeed, String price) {
        super(type, brand, model, price);
        this.coreCount = coreCount;
        this.clockSpeed = clockSpeed;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getCoreCount() + "," + this.getClockSpeed() + "," + this.getPriceAsString();
        return result;
    }


    public String getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(String coreCount) {
        this.coreCount = coreCount;
    }

    public String getClockSpeed() {
        return clockSpeed;
    }

    public void setClockSpeed(String clockSpeed) {
        this.clockSpeed = clockSpeed;
    }
}
