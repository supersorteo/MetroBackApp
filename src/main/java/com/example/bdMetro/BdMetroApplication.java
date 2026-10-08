package com.example.bdMetro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BdMetroApplication {

	public static void main(String[] args) {
		SpringApplication.run(BdMetroApplication.class, args);
	}

}
