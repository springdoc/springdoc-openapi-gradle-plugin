package com.example.demo.endpoints;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("null-values")
@RestController
public class NullValuesController {

	@GetMapping(value = "/null-values-api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
	public String apiDocs() {
		return "{\"openapi\":\"3.0.1\",\"paths\":{\"/\":{\"get\":{\"responses\":{\"404\":{\"description\":\"Not Found\","
				+ "\"content\":{\"*/*\":{\"example\":{\"errorMessage\":null}}}}}}}}}";
	}
}
