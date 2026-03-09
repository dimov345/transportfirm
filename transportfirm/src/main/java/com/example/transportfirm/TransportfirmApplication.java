package com.example.transportfirm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransportfirmApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransportfirmApplication.class, args);
	}
}
