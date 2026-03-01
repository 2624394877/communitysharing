package com.taoxin.communitysharing.search.business.config.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * Canal客户端配置，实现销毁方法
 */
@Component
@Slf4j
public class CanalClient implements DisposableBean{
    @Resource
    private CanalProperties canalProperties;

    private CanalConnector canalConnector;
    @Bean
    public CanalConnector getCanalConnector() {
        // 获取链接
        String address = canalProperties.getAddress();
        String[] addressList = address.split(":");
        // 获取IP地址
        String host = addressList[0];
        // 获取端口号
        int port = Integer.parseInt(addressList[1]);
        // 创建实例链接
        canalConnector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port), // IP地址和端口号
                canalProperties.getDestination(), // 数据源名称
                canalProperties.getUsername(), // 用户名
                canalProperties.getPassword() // 密码
        );
        // 连接
        canalConnector.connect();
        // 订阅
        canalConnector.subscribe(canalProperties.getSubscribe());
        // 回滚
        canalConnector.rollback();
        return canalConnector;
    }
    @Override
    public void destroy() throws Exception {
        // todo 将连接断开
        if (Objects.nonNull(canalConnector)) {
            canalConnector.disconnect();
        }
    }
}
