package com.emmes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.emmes.mapper")
public class HeashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(HeashopApplication.class, args);
	}

}
