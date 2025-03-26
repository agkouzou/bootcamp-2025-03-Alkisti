package dev.ctrlspace.bootcamp_2025_03.day_3.model;

public class Square {

    private double side;

    private double x;

    private double y;

    public Square() {
        x = 1;
        y = 1;
        side = 1;
    }

    public Square(double newSide) {
        x = 1;
        y = 1;
        side = newSide;
    }

    public Square(double newX, double newY, double newSide) {
        x = newX;
        y = newY;
        side = newSide;
    }

    public double getSide() {
        return side;
    }

    public void setSide(double side) {
        this.side = side;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


}
