package com.example.demo.endpoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/hello")
@RequestMapping("/hello")
public class HelloWorldController {

	@GetMapping("/world")
	@ResponseBody
	public String helloWorld() {
		return "Hello World!";
	}
}
