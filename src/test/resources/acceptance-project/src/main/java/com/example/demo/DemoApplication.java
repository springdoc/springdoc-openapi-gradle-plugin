package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.time.Duration;

@SpringBootApplication
public class DemoApplication {

	@Value("${slower:false}")
	boolean slower;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@PostConstruct
	public void afterBeanStuff() throws InterruptedException {
		if(slower) {
			Duration waitTime = Duration.ofSeconds(40);
			System.out.println("Waiting for " + waitTime + " before starting");
			Thread.sleep(waitTime.toMillis());
		}
	}

}
