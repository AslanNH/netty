package com.nh.netty.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class Constants {
    @Value("${netty.server.weight}")
    public static int weight;
    @Value("${netty.port}")
    public static int port;
    @Value("${server.path}")
    public static String SERVER_PATH;
}
