//package com.orcunbassimsek;

public class HardDrive extends Part {

    private String capacity;

    public HardDrive(String type, String brand, String model, String capacity, String price) {
        super(type, brand, model, price);
        this.capacity = capacity;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getCapacity() + "," + this.getPriceAsString();
        return result;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }
}
