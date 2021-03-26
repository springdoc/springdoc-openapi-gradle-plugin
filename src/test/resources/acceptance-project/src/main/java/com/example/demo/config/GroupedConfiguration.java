package com.example.demo;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("multiple-grouped-apis")
@Configuration
public class GroupedConfiguration {

    @Bean
    public GroupedOpenApi groupA() {
        return GroupedOpenApi.builder()
                             .group("groupA")
                             .pathsToMatch("/groupA/**")
                             .build();
    }

    @Bean
    public GroupedOpenApi groupB() {
        return GroupedOpenApi.builder()
                             .group("groupB")
                             .pathsToMatch("/groupB/**")
                             .build();
    }
}