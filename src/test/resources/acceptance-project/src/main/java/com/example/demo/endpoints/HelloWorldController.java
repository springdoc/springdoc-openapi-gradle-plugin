package com.example.demo.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/hello")
public class HelloWorldController {

    @GetMapping("/world")
    public String helloWorld(){
        return "Hello World!";
    }
}
