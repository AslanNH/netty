package com.nh.netty.client;

import com.nh.netty.zookeeper.ServerChangeWatcher;
import io.netty.channel.ChannelFuture;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

// 线程安全链路管理器
public class ChannelFutureManager {

    public static CopyOnWriteArrayList<String> serverList = new CopyOnWriteArrayList<>();

    public static AtomicInteger position = new AtomicInteger(0);

    public static CopyOnWriteArrayList<ChannelFuture> channelFutures = new CopyOnWriteArrayList<>();

    public static ChannelFuture get() throws Exception{
        ChannelFuture channelFuture = get(position);
        if(channelFuture == null){
            ServerChangeWatcher.initChannelFuture();
        }
        return get(position);
    }
    private static ChannelFuture get(AtomicInteger i){
        int size = channelFutures.size();
        if(size==0){
            return null;
        }
        ChannelFuture channel = null;
        synchronized (i){
            if(i.get()>=size){
                channel = channelFutures.get(0);
                i.set(0);
            }else{
                channel = channelFutures.get(i.getAndIncrement());
            }
            if(!channel.channel().isActive()){
                channelFutures.remove(channel);
                return get(position);

            }
        }
        return channel;
    }
    public static void removeChannel(ChannelFuture channel){
        channelFutures.remove(channel);
    }
    public static void add(ChannelFuture channelFuture){
        channelFutures.add(channelFuture);
    }
    public static void clear(){
        for(ChannelFuture future:channelFutures){
            future.channel().close();
        }
        channelFutures.clear();
    }

    public static void addAll(List<ChannelFuture> futures){
        for(ChannelFuture channelFuture:futures){
            add(channelFuture);
        }
    }
}
