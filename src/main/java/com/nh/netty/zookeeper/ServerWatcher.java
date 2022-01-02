package com.nh.netty.zookeeper;

import com.nh.netty.constant.Constants;
import com.nh.netty.server.NettyServer;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerWatcher implements CuratorWatcher {
    public static String serverKey = "";
    public static ServerWatcher serverWatcher = null;

    public static ServerWatcher getInstance(){
        if(serverWatcher==null){
            serverWatcher = new ServerWatcher();
        }
        return serverWatcher;
    }

    /**
     * 当本机与zksession断掉就会触发，然后重建临时节点
     * @param watchedEvent
     * @throws Exception
     */
    @Override
    public void process(WatchedEvent watchedEvent) throws Exception {
        System.out.println("===========服务器监听Zookeeper=event==="+watchedEvent.getState()+"===" +
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        if(watchedEvent.getState().equals(Watcher.Event.KeeperState.Disconnected)
            || watchedEvent.getState().equals(Watcher.Event.KeeperState.Expired)){
            try{
                try{
                    ZookeeperFactory.create().close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                CuratorFramework client = ZookeeperFactory.recreate();
                client.getChildren().usingWatcher(this).forPath(NettyServer.SERVER_PATH);
                InetAddress inetAddress = InetAddress.getLocalHost();
                Stat stat = client.checkExists().forPath(NettyServer.SERVER_PATH);
                if(stat ==null) {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT.PERSISTENT).forPath(NettyServer.SERVER_PATH, "0".getBytes());
                }
                client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(NettyServer.SERVER_PATH+"/"+inetAddress.getHostAddress()
                        +"#"+Constants.port+"#"+ Constants.weight+"#");
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            CuratorFramework client = ZookeeperFactory.create();
            client.getChildren().usingWatcher(this).forPath(NettyServer.SERVER_PATH);
        }
    }
}
