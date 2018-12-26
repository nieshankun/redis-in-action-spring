package com.example.redisdemo.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @author nsk
 * 2018/12/13 8:04
 */
@Configuration
@EnableConfigurationProperties(JedisProperties.class)
public class JedisClusterConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JedisClusterConfig.class);

    @Autowired
    private JedisProperties jedisProperties;

    private int maxTotal = 100;
    private int maxIdle = 5;
    private int maxWaitMills = 1000;
    private int soTimeout = 5000;
    private int maxAttempts = 5;

    @Bean
    public JedisCluster getJedisCluster() {
        String[] serverArray = jedisProperties.getClusterNodes().split(",");
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxWaitMillis(maxWaitMills);
        LOGGER.info("***************" + jedisProperties.getClusterNodes());
        Set<HostAndPort> nodes = new HashSet<>();
        for (String ipPort : serverArray) {
            String[] ipPortPair = ipPort.split(":");
            nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
        }
        return new JedisCluster(nodes, jedisProperties.getCommandTimeout(), soTimeout, maxAttempts,
                jedisProperties.getPassword(), poolConfig);
    }
}
