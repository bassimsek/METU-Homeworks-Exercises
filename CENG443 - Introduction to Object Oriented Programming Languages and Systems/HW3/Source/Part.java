//package com.orcunbassimsek;

public abstract class Part {

    private String type;
    private String brand;
    private String model;
    private String price;


    public Part(String type, String brand, String model, String price) {
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.price = price;
    }

    @Override
    public String toString() {

        String result = this.getType() + "," + this.getBrand() + "," + this.getModel();
        return result;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPriceAsString() {
        return price;
    }

    public void setPriceAsString(String price) {
        this.price = price;
    }

    public double getPriceAsDouble() {
        double priceAsDouble = Double.parseDouble(price.split(" ")[0]);
        return priceAsDouble;
    }

}
