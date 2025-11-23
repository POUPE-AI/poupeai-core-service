package io.github.poupeai.core.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.poupeai.core")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
