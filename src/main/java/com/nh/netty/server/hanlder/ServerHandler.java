package com.nh.netty.server.hanlder;

import com.alibaba.fastjson.JSONObject;
import com.nh.netty.component.RequestFuture;
import com.nh.netty.component.Response;
import com.nh.netty.server.Mediator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// Sharable表示此handler对所有channel共享，无状态，但是有线程安全问题
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取客户端发送的数据
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       RequestFuture request = JSONObject.parseObject(msg.toString(),RequestFuture.class);
       Response response = Mediator.process(request);
       System.out.println("请求消息为==="+msg.toString());
       ctx.channel().writeAndFlush(JSONObject.toJSONString(response));
    }
}
