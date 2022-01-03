package com.nh.netty.server.commandLine;

import com.nh.netty.annotation.Remote;
import com.nh.netty.server.Mediator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 在启动springboot之后，
 * 将所有包含有@Controller注解的@Remote注解的类和方法加载到Mediator
 * 先于启动netty执行
 */
@Component
public class InitLoadRemoteMethod implements
        ApplicationListener<ContextRefreshedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 获取spring容器中所有@Controller注解的对象
        Map<String,Object> controllerBeans
                = event.getApplicationContext()
                        .getBeansWithAnnotation(Remote.class);

        // 遍历存储到自定义的netty管理的容器中
        for(String key: controllerBeans.keySet()){
            Object bean = controllerBeans.get(key);
            Method[] methods = bean.getClass().getDeclaredMethods();
            for(Method method: methods){
                String methodVal = bean.getClass().getInterfaces()[0].getName()+"."+method.getName();
                Mediator.MethodBean methodBean = new Mediator.MethodBean();
                methodBean.setBean(bean);
                methodBean.setMethod(method);

                Mediator.MethodBeans.put(methodVal,methodBean);
            }

        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
