//package com.orcunbassimsek;

public class PSU extends Part {

    private String formFactor;
    private String wattage;


    public PSU(String type, String brand, String model, String formFactor, String wattage, String price) {
        super(type, brand, model, price);
        this.formFactor = formFactor;
        this.wattage = wattage;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getFormFactor() + "," + this.getWattage() + "," + this.getPriceAsString();
        return result;
    }

    public String getFormFactor() {
        return formFactor;
    }

    public void setFormFactor(String formFactor) {
        this.formFactor = formFactor;
    }

    public String getWattage() {
        return wattage;
    }

    public void setWattage(String wattage) {
        this.wattage = wattage;
    }
}
