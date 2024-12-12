package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication(scanBasePackages = "com")
@MapperScan(basePackages = "com.rk.mapper")
@EnableScheduling
public class TcpApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }
    /**
     * 因为定时器默认创建的线程池为一个线程的所以这里需要改成多个线程的，不然相同时间段任务执行时会等待,因为只有一个线程在工作
     * @return
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }
}
