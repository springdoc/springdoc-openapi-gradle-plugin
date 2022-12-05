package com.example.demo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;

@SpringBootApplication
public class DemoApplication implements ApplicationRunner{

    @Value("${slower:false}")
    boolean slower;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
    @Override
    public void run(ApplicationArguments arg0) throws Exception {
        System.out.println("Hello World from Application Runner");
        if (slower) {
            Duration waitTime = Duration.ofSeconds(40);
            System.out.println("Waiting for " + waitTime + " before starting");
            Thread.sleep(waitTime.toMillis());
        }
    }

}
