package com.nh.netty.client.proxy;

import com.alibaba.fastjson.JSONObject;
import com.nh.netty.annotation.RemoteInvoke;
import com.nh.netty.client.NettyClient;
import com.nh.netty.component.RequestFuture;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class JdkProxy implements InvocationHandler, BeanPostProcessor {

    private Field target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestFuture requestFuture = new RequestFuture();
        requestFuture.setPath(target.getType().getName()+"."+method.getName());
        requestFuture.setRequest(args[0]);
        Object resp = NettyClient.sendRequest(requestFuture);

        Class returnType = method.getReturnType();
        if(resp == null){
            return null;
        }
        resp = JSONObject.parseObject(JSONObject.toJSONString(resp),returnType);
        return resp;
    }

    private Object getJdkProxy(Field field){
        this.target = field;
        return Proxy.newProxyInstance(field.getType().getClassLoader(),new Class[]{field.getType()},this);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
       Field[] fields = bean.getClass().getDeclaredFields();
       for(Field field: fields){
           if(field.isAnnotationPresent(RemoteInvoke.class)){
               field.setAccessible(true);
               try{
                   field.set(bean,getJdkProxy(field));
               }catch (Exception e){
                   e.printStackTrace();;
               }
           }
       }
       return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
       return bean;
    }
}
