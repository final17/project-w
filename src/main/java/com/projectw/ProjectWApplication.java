package com.projectw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ProjectWApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectWApplication.class, args);
    }

}
