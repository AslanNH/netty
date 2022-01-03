package com.nh.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.nh.netty.client.controller.LoginController;
import com.nh.netty.component.RequestFuture;
import com.nh.netty.client.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.nio.charset.Charset;

public class NettyClient {

    public static EventLoopGroup group = new NioEventLoopGroup();

    public static Bootstrap getBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        // 内存分配器
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        final ClientHandler handler = new ClientHandler();

        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(
                        new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                0,4,0,4)
                );
                nioSocketChannel.pipeline().addLast(new StringDecoder());
                nioSocketChannel.pipeline().addLast(handler);
                nioSocketChannel.pipeline().addLast(new LengthFieldPrepender(4,false));
                nioSocketChannel.pipeline().addLast(new
                        StringEncoder(Charset.forName("utf-8")));

            }
        });
        return bootstrap;
    }

    public Object sendRequest(Object msg,String path)throws Exception{
        try{
            RequestFuture request = new RequestFuture();
            request.setRequest(msg);
            request.setPath(path);
            String requestStr = JSONObject.toJSONString(request);
            ChannelFuture future = ChannelFutureManager.get();
            future.channel().writeAndFlush(requestStr);
            Object result = request.get();
            return result;
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }
    public static void main(String[] args) throws Exception{
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                new String[]{"com.nh.netty.client.**","com.nh.netty.annotation"
                        ,"com.nh.netty.component","com.nh.netty.constant","com.nh.netty.zookeeper"});
        LoginController loginController = context.getBean(LoginController.class);
        Object result = loginController.getUserByName("张三");
        System.out.println(result);

    }

    public static Object sendRequest(RequestFuture request)throws Exception{
        try{
            String requestStr = JSONObject.toJSONString(request);
            ChannelFuture future = ChannelFutureManager.get();
            future.channel().writeAndFlush(requestStr);

            Object result = request.get();
            return result;
        }catch ( Exception e){
            e.printStackTrace();;
            throw e;
        }
    }
}
