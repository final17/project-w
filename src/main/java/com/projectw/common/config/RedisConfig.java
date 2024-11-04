package com.projectw.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.TransportMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RedisConfig {

    @Value("${redis.cluster.nodes}")
    private String redisClusterNodes;

    private static final String REDISSON_HOST_PREFIX = "redis://";

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        List<String> nodes = Arrays.stream(redisClusterNodes.trim().split(",")).toList();
        List<RedisNode> redisNodes = nodes.stream()
                .map(node -> {
                    String[] parts = node.split(":");
                    return new RedisNode(parts[0], Integer.parseInt(parts[1]));
                }).toList();

        RedisClusterConfiguration redisConfiguration = new RedisClusterConfiguration();
        redisConfiguration.setClusterNodes(redisNodes);
        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedissonClient redissonClient() {
        // 클러스터 노드 주소 목록 설정
        List<String> nodes = Arrays.stream(redisClusterNodes.trim().split(","))
                .map(node -> REDISSON_HOST_PREFIX + node)
                .collect(Collectors.toList());

        // Redisson 클러스터 설정
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO) // 성능 최적화 설정
                .useClusterServers()
                .setSslEnableEndpointIdentification(true) // SSL 검증 활성화
                .setScanInterval(2000)                   // 클러스터 노드 스캔 간격
                .setConnectTimeout(10000)                // 연결 타임아웃 (밀리초)
                .setRetryAttempts(3)                     // 연결 재시도 횟수
                .setRetryInterval(1500)                  // 재시도 간격 (밀리초)
                .addNodeAddress(nodes.toArray(new String[0])); // 클러스터 노드 주소 설정

        return Redisson.create(config);
    }
}
