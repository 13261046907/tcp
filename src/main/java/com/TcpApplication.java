package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan(basePackages = "com.rk.mapper")
@EnableScheduling
public class TcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }

}
