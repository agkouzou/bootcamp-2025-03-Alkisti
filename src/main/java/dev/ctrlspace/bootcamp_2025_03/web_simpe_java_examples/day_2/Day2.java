package dev.ctrlspace.bootcamp_2025_03.web_simpe_java_examples.day_2;

public class Day2 {

    public static void main(String[] args) {

        System.out.println("Hello, World!");

        int x = 8;
        System.out.println(x);
        System.out.println("x = " + x);

//      ERROR:
//        int y;
//        System.out.println("y = " + y);

        double y = 3.14;
//        "y = " + y
//        "y = " + 3.14
//        "y = " + "3.14"
//        "y = 3.14"

        System.out.println("y = " + y);

        System.out.println( "x + y = " + (x + y) );

        System.out.println("double cast to int: " + (int) 3.999 );

        String name = "John";

        System.out.println("name = " + name);


        int[] a = {6, 7, 8, 9, 10};
        int[] b = new int[5];
        b[0] = 11;
        b[1] = 12;
        b[2] = 13;
        b[3] = 14;
        b[4] = 15;

        int[] c = new int[5];
        for (int i = 0; i < c.length; i++) {
            c[i] = i+1;
        }


//        System.out.print("{");
//        for (int i = 0; i < c.length; i++) {
//            System.out.print(c[i]);
//            if (i < c.length - 1) {
//                System.out.print(", ");
//            }
//        }
//        System.out.println("}");


//        System.out.print("a: ");
//        printMatrix(a);
//        System.out.print("b: ");
//        printMatrix(b);
//        System.out.print("c: ");
//        printMatrix(c);

        System.out.println("a: " + matrixToString(a)); // "a: " + "{6, 7, 8, 9, 10}"
        System.out.println("b: " + matrixToString(b));
        System.out.println("c: " + matrixToString(c));

    }


    public static void printMatrix(int[] a)
    {
        System.out.print("{");
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) {
                System.out.print(", ");

            }
        }
        System.out.println("}");
    }

    public static String matrixToString(int[] a)
    {
        String str = "{";
        for (int i = 0; i < a.length; i++) {
            str = str + a[i];
            if (i < a.length - 1) {
                str = str + ", ";
            }
        }
        str = str + "}";
        return str;
    }


}
