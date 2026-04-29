package com.sgp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SgpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgpApplication.class, args);
	}

}
