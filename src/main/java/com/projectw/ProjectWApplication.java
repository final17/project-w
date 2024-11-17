package com.projectw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class ProjectWApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectWApplication.class, args);
    }

}
