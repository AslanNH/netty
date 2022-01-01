package com.nh.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.nh.netty.component.RequestFuture;
import com.nh.netty.component.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;

@Data
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = JSONObject.parseObject(msg.toString(),Response.class);

        RequestFuture.received(response);

    }

}
