package com.example.demo.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(value = {"some.second.property"}, havingValue = "someValue")
@RestController("/conditional")
public class ConditionalController {

    @GetMapping("/conditional")
    public String conditional(){
        return "conditional";
    }
}