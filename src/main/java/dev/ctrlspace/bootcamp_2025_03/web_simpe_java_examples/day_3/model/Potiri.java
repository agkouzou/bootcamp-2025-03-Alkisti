package dev.ctrlspace.bootcamp_2025_03.web_simpe_java_examples.day_3.model;

public class Potiri {

    private double height;
    private double radius;

    private double volume;

    private String barcode;

    public Potiri() {
    }


    public Potiri(double height, double radius, double volume, String barcode) {
        this.height = height;
        this.radius = radius;
        this.volume = volume;
        this.barcode = barcode;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }


    @Override
    public String toString() {
        return "Potiri{" +
                "height=" + height +
                ", radius=" + radius +
                ", volume=" + volume +
                ", barcode='" + barcode + '\'' +
                '}';
    }
}
