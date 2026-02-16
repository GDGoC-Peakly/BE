package com.example.peakly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class PeaklyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeaklyApplication.class, args);
    }

}
