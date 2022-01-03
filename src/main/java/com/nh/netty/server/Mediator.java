package com.nh.netty.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nh.netty.component.RequestFuture;
import com.nh.netty.component.Response;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存所有接口的对象和方法。
 */
public class Mediator {

    public static Map<String,MethodBean> MethodBeans;

    static{
        MethodBeans = new HashMap<>();
    }

    /**
     * 请求分发处理
     * @param requestFuture
     * @return
     */
    public static Response process(RequestFuture requestFuture){
        Response response = new Response();
        try{
            String path = requestFuture.getPath();
            MethodBean methodBean = MethodBeans.get(path);
            if(methodBean!=null){
                Object bean = methodBean.getBean();
                Method method = methodBean.getMethod();
                Object body = requestFuture.getRequest();

                Class[] paramTypes = method.getParameterTypes();
                Class  paramType = paramTypes[0];
                Object param = null;
                if(paramType.isAssignableFrom(List.class)){
                    param = JSONArray.parseArray(JSONArray.toJSONString(body),paramType);
                }else if(paramType.getName().equals(String.class.getName())){
                    param = body;
                }else{
                    param = JSONObject.parseObject(JSONObject.toJSONString(body),paramType);
                }
                Object result = method.invoke(bean,param);
                response.setResult(result);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        response.setId(requestFuture.getId());
        return response;
    }

    public  static class MethodBean {

        private Object bean;

        private Method method;

        public Object getBean() {
            return bean;
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }
}
