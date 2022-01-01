package com.nh.netty.component;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class RequestFuture {

    private static final AtomicLong aid = new AtomicLong(1);

    public RequestFuture(){
        id = aid.incrementAndGet();
        addFuture(this);
    }
    public static Map<Long,RequestFuture> futures
        = new ConcurrentHashMap<Long,RequestFuture>();

    private long id;
    private Object request;
    private Object result;
    private long timeout = 5000;
    private String path;

    public static void addFuture(RequestFuture future){
        futures.put(future.getId(),future);
    }

    public Object get(){
        synchronized (this){
            while(this.result == null){
                try{
                    this.wait(timeout);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return this.result;
    }

    public static void received(Response resp){
        RequestFuture future = futures.remove(resp.getId());
        if(future!=null){
            future.setResult(resp.getResult());
        }
        synchronized (future){
            future.notify();
        }
    }
}
