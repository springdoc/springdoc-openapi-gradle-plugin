package com.example.demo.endpoints;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("multiple-grouped-apis")
@RestController("/grouped")
public class GroupedController {

    @GetMapping("/groupA")
    public String groupA(){
        return "groupA";
    }

    @GetMapping("/groupB/first")
    public String groupB_first(){
        return "groupB_first";
    }

    @GetMapping("/groupB/second")
    public String groupB_second(){
        return "groupB_second";
    }
}
