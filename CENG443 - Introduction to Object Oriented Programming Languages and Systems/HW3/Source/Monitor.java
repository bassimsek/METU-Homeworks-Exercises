//package com.orcunbassimsek;

public class Monitor extends Part {

    private String aspectRatio;
    private String size;

    public Monitor(String type, String brand, String model, String aspectRatio, String size, String price) {
        super(type, brand, model, price);
        this.aspectRatio = aspectRatio;
        this.size = size;
    }

    @Override
    public String toString() {

        String firstResult = super.toString();
        String result = firstResult + "," + this.getAspectRatio() + "," + this.getSize() + "," + this.getPriceAsString();
        return result;
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
