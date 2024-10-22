package com.tcp;

import com.config.RedisUtil;
import com.rk.service.DeviceInstanceService;
import com.rk.service.DeviceTcpInstanceService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

@Slf4j
@Component
public class NettyTcpServer implements ApplicationRunner {

    private final RedisUtil redisUtil;
    private final DeviceTcpInstanceService deviceTcpInstanceService;
    private final DeviceInstanceService deviceInstanceService;
    // 构造函数注入RedisUtil
    public NettyTcpServer(RedisUtil redisUtil, DeviceInstanceService deviceInstanceService, DeviceTcpInstanceService deviceTcpInstanceService) {
        this.redisUtil = redisUtil;
        this.deviceTcpInstanceService = deviceTcpInstanceService;
        this.deviceInstanceService = deviceInstanceService;
    }

    public void start(InetSocketAddress address) {
        //配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    // 绑定线程池
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(address)
                    //编码解码
                    .childHandler(new NettyTcpServerChannelInitializer(redisUtil, deviceInstanceService, deviceTcpInstanceService))
                    //服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //保持长连接，2小时无数据激活心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind(address).sync();
            log.info("netty tcp服务器开始监听端口：" + address.getPort());
            //关闭channel和块，直到它被关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        InetSocketAddress tcpAddress = new InetSocketAddress("0.0.0.0", 1883);
        this.start(tcpAddress);
    }
}
