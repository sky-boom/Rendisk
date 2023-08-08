package com.wzr.rendisk.test;


public class MyTest {
    public static void main(String[] args) throws InterruptedException {
        int num = 0;
        while (true) {
            System.out.println(num ++);
            Thread.sleep(1000);
        }
    }
}
