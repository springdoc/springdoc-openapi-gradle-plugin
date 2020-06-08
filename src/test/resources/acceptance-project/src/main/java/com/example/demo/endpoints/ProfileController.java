package com.example.demo.endpoints;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("multiple-endpoints")
@RestController("/special")
public class ProfileController {

    @Value("${test.props}")
    String profileRelatedValue;

    @GetMapping("/")
    public String special(){
        return profileRelatedValue;
    }
}
