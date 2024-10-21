package com.projectw.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@Configuration
public class RedisConfig {

    @Value("${redis.cluster.nodes}")
    private String redisClusterNodes;

    private static final String REDISSON_HOST_PREFIX = "redis://";

    @Bean
    public RedissonClient redissonClient() {
        List<String> nodes = Arrays.stream(redisClusterNodes.trim().split(",")).collect(Collectors.toList());

        for (int i = 0; i < nodes.size(); i++) {
            nodes.set(i, REDISSON_HOST_PREFIX + nodes.get(i));
        }

        Config config = new Config();
        config.useClusterServers()
            .setSslEnableEndpointIdentification(true)
            .setScanInterval(2000)
            .setNodeAddresses(nodes);
        return Redisson.create(config);
    }
}
