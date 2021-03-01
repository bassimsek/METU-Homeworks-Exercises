//package com.orcunbassimsek;

public class Keyboard extends Part {

    private String connectionType;


    public Keyboard(String type, String brand, String model, String connectionType, String price) {
        super(type, brand, model, price);
        this.connectionType = connectionType;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getConnectionType() + "," + this.getPriceAsString();
        return result;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
}
