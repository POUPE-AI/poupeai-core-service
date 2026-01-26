package io.github.poupeai.core.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "io.github.poupeai.core")
@EntityScan(basePackages = "io.github.poupeai.core.persistence.entity")
@EnableJpaRepositories(basePackages = "io.github.poupeai.core.persistence.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
