package com.example.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class ConsumerApplicationTests {

    @Test
    void contextLoads() {
    }

    public static void main(String[] args) {
        int num = 13 & Integer.MAX_VALUE;
        System.out.println("num："+(num%2));
    }

}
