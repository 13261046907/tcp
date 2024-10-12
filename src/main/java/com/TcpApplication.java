package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan(basePackages = "com.rk.mapper")
public class TcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }

}
