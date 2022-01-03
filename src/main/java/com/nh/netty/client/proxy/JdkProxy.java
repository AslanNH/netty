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

    /**
     * 调用的时候执行的代理操作
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RequestFuture requestFuture = new RequestFuture();
        // UserService.getUserByName(String userName)
        requestFuture.setPath(target.getType().getName()+"."+method.getName());
        // 设置参数——userName
        requestFuture.setRequest(args[0]);
        // 使用netty发送请求
        Object resp = NettyClient.sendRequest(requestFuture);
        // 获取返回类型
        Class returnType = method.getReturnType();
        if(resp == null){
            return null;
        }
        // 类型转换
        resp = JSONObject.parseObject(JSONObject.toJSONString(resp),returnType);
        return resp;
    }

    private Object getJdkProxy(Field field){
        this.target = field;
        return Proxy.newProxyInstance(field.getType().getClassLoader(),new Class[]{field.getType()},this);
    }

    /**
     * 初始化bean之前，对其有RemoteInvoker注解的属性类设置代理，
     * 这里就是对loginController的UserService类设置代理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
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
