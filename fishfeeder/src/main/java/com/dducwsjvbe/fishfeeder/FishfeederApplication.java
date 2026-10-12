package com.dducwsjvbe.fishfeeder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan({"com.dducwsjvbe.fishfeeder", "com.dducwsjvbe.common_service"})
public class FishfeederApplication {

	public static void main(String[] args) {
		SpringApplication.run(FishfeederApplication.class, args);
	}

}
