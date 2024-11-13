package com.lanlan.mock.mainpricegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling // 开启 @Scheduled 定时任务
@SpringBootApplication
public class MainPriceGenApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainPriceGenApplication.class, args);
	}

}
