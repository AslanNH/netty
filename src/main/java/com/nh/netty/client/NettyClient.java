package com.nh.netty.client;

import com.alibaba.fastjson.JSONObject;
import com.nh.netty.component.RequestFuture;
import com.nh.netty.handler.ClientHandler;
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
       NettyClient client = new NettyClient();
       for(int i=0;i<100;i++){
           Object result = client.sendRequest("id"+i,"getUserNameById");
           System.out.println(result);
       }

    }
}
