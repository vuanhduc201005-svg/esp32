package com.dducwsjvbe.web_ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan({"com.dducwsjvbe.web_ui","com.dducwsjvbe.common_service"})
public class WebUiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebUiApplication.class, args);
	}

}
