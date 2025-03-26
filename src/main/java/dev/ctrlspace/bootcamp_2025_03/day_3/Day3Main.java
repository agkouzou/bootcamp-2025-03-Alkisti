package dev.ctrlspace.bootcamp_2025_03.day_3;

import dev.ctrlspace.bootcamp_2025_03.day_3.model.Circle;
import dev.ctrlspace.bootcamp_2025_03.day_3.model.Message;

import java.time.Instant;

public class Day3Main {


    public static void main(String[] args) {

        int[] x = new int[10];
        Circle c1 = new Circle();




        Circle c2 = new Circle(5, 7, 0);

        Circle c3 = new Circle(3);
        Circle c4 = new Circle("hello!!!");



        System.out.println("c1.y = " + c1.getY());
        System.out.println("c2.y = " + c2.getY());



        System.out.println(c1.toString());
        System.out.println(c2);



        System.out.println("Area: " + c1.getArea());
        System.out.println("Contain (2,2): " + c1.containsPoint(2, 2)); // false
        System.out.println("Contain (1.5,1): " + c1.containsPoint(1.5, 1)); // true


        System.out.println("Generating random messages...");

        Message[] messages = generateRandomMessages(5000, "John");

        System.out.println("Messages generated:");

//        for (int i = 0; i < messages.length; i++) {
//
//            System.out.println(messages[i]);
//        }

        for (Message message : messages) {
            System.out.println(message);
        }


    }

    public static Message[] generateRandomMessages(int count, String sender) {

        Message[] messages = new Message[count];

        for (int i = 0; i < count; i++) {
            messages[i] = new Message("Message #" + (i+1), sender, Instant.now());
        }

        return messages;

    }


}
