package com.nh.netty;

import com.nh.netty.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@ComponentScans(value = { @ComponentScan(value = "com.nh.netty.annotation")
        ,@ComponentScan(value = "com.nh.netty.component")
        ,@ComponentScan(value = "com.nh.netty.constant")
        ,@ComponentScan(value = "com.nh.netty.server.**")
        ,@ComponentScan(value = "com.nh.netty.zookeeper")})
@SpringBootApplication
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();
    }

}
