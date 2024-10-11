package com;

import com.tcp.NettyTcpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetSocketAddress;

@SpringBootApplication(scanBasePackages = "com")
@EnableAsync
public class TcpApplication implements CommandLineRunner {

    @Autowired
    NettyTcpServer nettyTcpServer;

    public static void main(String[] args) {
        SpringApplication.run(TcpApplication.class, args);
    }

    /**
     * 启动netty 服务
     * tcp和udp同时只能启动一个
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        //启动TCP服务
        InetSocketAddress tcpAddress = new InetSocketAddress(DefaultConstants.SOCKET_IP,DefaultConstants.TCP_SOCKET_PORT);
        nettyTcpServer.start(tcpAddress);
    }
}
