package com.nh.netty.annotation;


import java.lang.annotation.*;

// 注解远程调用的接口属性
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RemoteInvoke {
}
