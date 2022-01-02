package com.nh.netty.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class Constants {
    @Value("${netty.server.weight}")
    public static int weight =1;
    @Value("${netty.port}")
    public static int port = 9999;
    @Value("${server.path}")
    public static String SERVER_PATH = "/netty";
}
