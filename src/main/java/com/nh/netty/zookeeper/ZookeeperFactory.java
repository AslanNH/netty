package com.nh.netty.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;

public class ZookeeperFactory {
    @Value("${zookeeper.ip}")
    private static String ip;
    @Value("${zookeeper.port}")
    private static int port;

    public static CuratorFramework client;
    public static CuratorFramework create(){
        if(client == null){
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            client = CuratorFrameworkFactory.newClient("localhost:2181",1000,5000,retryPolicy);
            client.start();
        }
        return client;
    }

    // 会话丢失重新创建
    public static CuratorFramework recreate(){
        client = null;
        create();
        return client;
    }

    public static void main(String[]args) throws  Exception{
        CuratorFramework client = create();
        client.create().forPath("/netty");
    }


    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
