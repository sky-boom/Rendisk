package com.wzr.rendisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class RendiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(RendiskApplication.class, args);
	}

}
