package com.skillgapai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Spring Boot app. Run with: mvnw spring-boot:run
 *
 * Component scanning starts from this package (com.skillgapai), so it
 * covers entity/repository/service/controller classes added in later
 * phases without needing extra configuration.
 */
@SpringBootApplication
public class SkillGapAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillGapAiApplication.class, args);
    }
}
