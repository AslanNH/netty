package com.nh.netty;

import com.nh.netty.server.NettyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettyApplication.class, args);
        NettyServer nettyServer = new NettyServer();
        nettyServer.start();
    }

}
