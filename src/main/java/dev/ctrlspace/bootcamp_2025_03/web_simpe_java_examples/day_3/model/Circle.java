package dev.ctrlspace.bootcamp_2025_03.web_simpe_java_examples.day_3.model;

public class Circle {

    private double radius;
    private double x;
    private double y;

    public Circle() {
        x = 1;
        y = 1;
        radius = 1;
    }

    public Circle(double newRadius) {
        x = 1;
        y = 1;
        radius = newRadius;
    }

    public Circle(String str) {
        // ....
    }

    public Circle(double newX, double newY, double newRadius ) {
        x = newX;
        y = newY;
        radius = newRadius;
    }



    ////// setters and getters

    public double getRadius() {
        return radius;
    }

    public void setRadius(double newRadius) {
        radius = newRadius;
    }

    public double getX() {
        return x;
    }

    public void setX(double newX) {
        x = newX;
    }

    public double getY() {
        return y;
    }

    public void setY(double newY) {
        y = newY;
    }

    public String toString() {
        return "Circle with radius " + radius + " at (" + x + ", " + y + ")";
    }

    public double getArea() {
        return Math.PI * radius * radius;
    }

    public double getCircumference() {
        return 2 * Math.PI * radius;
    }


    public boolean containsPoint(double x, double y) {
        return Math.sqrt((this.x - x) * (this.x - x) + (this.y - y) * (this.y - y)) < radius;
    }





}
