package com.example.transportfirm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TransportfirmApplication {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Sofia"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TransportfirmApplication.class, args);
	}
}
