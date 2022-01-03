package com.nh.netty.server;

import com.nh.netty.constant.Constants;
import com.nh.netty.server.hanlder.ServerHandler;
import com.nh.netty.server.watcher.ServerWatcher;
import com.nh.netty.zookeeper.ZookeeperFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class NettyServer {
    public static final String SERVER_PATH = "/netty";

    public static void main(String[] args)throws  Exception {
        start();
    }
    public static void start() {
        // 1.新建两个线程组，boss线程组启动一条线成，监听OP_ACCEPT事件
        // worker线程组默认启动CPU核数*2的线程(IO密集型)监听客户端连接的OP_READ和OP_WRITE事件，处理IO事件

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            // ServerBootStrap 是netty服务启动辅助类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup);
            // 设置TCP Socker通道为NioServerSocketChannel
            // UDP通信，则设置为DatagramChannel
            serverBootstrap.channel(NioServerSocketChannel.class);
            // 设置TCP参数
            serverBootstrap.option(ChannelOption.SO_BACKLOG,128)
                    .childHandler(new ChannelInitializer<SocketChannel>(){

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE
                            ,0,4,0,4));
                            // 将接受到的ByteBuf数据包转换为string
                            socketChannel.pipeline().addLast(new StringDecoder());
                            // 向worker线程的管道双向链表中添加处理类ServerHandler
                            // 整个处理流程:headContext->channelRead->serverhandler->channelread
                            // 读取数据进行业务逻辑判断，最后返回给客户端->tailcontext-write->headcontext-write
                            socketChannel.pipeline().addLast(new ServerHandler());
                            socketChannel.pipeline().addLast(new LengthFieldPrepender(4,false));
                            // 将字符串消息转换ByteBuf
                            socketChannel.pipeline().addLast(new StringEncoder());

                        }
                    });
            // 同步绑定端口
            ChannelFuture future = serverBootstrap.bind(9999).sync();

            // 链接zk
            CuratorFramework client = ZookeeperFactory.create();
            InetAddress inetAddress = InetAddress.getLocalHost();
            Stat stat = client.checkExists().forPath(SERVER_PATH);
            if(stat == null){
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(SERVER_PATH,"0".getBytes());
            }
            // 在SERVER_PATH目录下构建临时节点，127.0.0.1#8080#1#
            client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(SERVER_PATH+"/"+inetAddress.getHostAddress()
            +"#"+Constants.port+"#"+Constants.weight+"#");
            // 加上zk监控，以防Session终端导致临时节点丢失
            ServerWatcher.serverKey=inetAddress.getHostAddress()+Constants.port+ Constants.weight;
            client.getChildren().usingWatcher(ServerWatcher.getInstance()).forPath(SERVER_PATH);

            // 阻塞主线程，直到socket通道被关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();

        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
