package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan(basePackages = "com.insshopy.hype.center.mapper")
public class TcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }

}
