package io.prada.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PradaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PradaApplication.class, args);
    }

    @RestController
    class HelloClass {
        @GetMapping("/hello")
        String hello() {
            return "HELLO WORLD";
        }
    }
}
