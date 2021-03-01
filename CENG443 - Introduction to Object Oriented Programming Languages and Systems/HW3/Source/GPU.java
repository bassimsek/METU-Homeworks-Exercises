//package com.orcunbassimsek;

public class GPU extends Part {

    private String chipset;
    private String capacity;
    private String clockSpeed;


    public GPU(String type, String brand, String model, String chipset, String capacity, String clockSpeed, String price) {
        super(type, brand, model, price);
        this.chipset = chipset;
        this.capacity = capacity;
        this.clockSpeed = clockSpeed;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getChipset() + "," + this.getCapacity() + "," + this.getClockSpeed() + "," + this.getPriceAsString();
        return result;
    }

    public String getChipset() {
        return chipset;
    }

    public void setChipset(String chipset) {
        this.chipset = chipset;
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
