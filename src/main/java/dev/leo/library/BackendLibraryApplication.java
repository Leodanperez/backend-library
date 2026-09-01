package dev.leo.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendLibraryApplication.class, args);
    }

}
