package com.projectw.domain.redis;

import com.projectw.domain.notification.client.MessageClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisHealthCheckScheduler {

    private final RedissonClient redissonClient;
    private final MessageClient slackClient;

    @Value("${REDIS_HEALTH_SLACK_WEBHOOK_URL}")
    private String redisHealthCheckHookUrl;

    @Value("${redis.cluster.nodes}")
    private String redisClusterNodes;

    private static final String REDISSON_HOST_PREFIX = "redis://";
    private final List<String> nodes = new ArrayList<>();
    private int prevSize;

    @PostConstruct
    public void init() {
        nodes.addAll(Arrays.stream(redisClusterNodes.trim().split(",")).toList());

        for (int i = 0; i < nodes.size(); i++) {
            nodes.set(i, REDISSON_HOST_PREFIX + nodes.get(i));
        }
        prevSize = nodes.size();
    }

    @Scheduled(fixedRate = 60000) // 1분마다 상태 체크
    public void checkClusterHealth() {
        RedisCluster redisCluster = redissonClient.getRedisNodes(RedisNodes.CLUSTER);

        Collection<RedisClusterMaster> masters = redisCluster.getMasters();
        Collection<RedisClusterSlave> slaves = redisCluster.getSlaves();
        Collection<RedisNode> redisNodes = new ArrayList<>();
        redisNodes.addAll(masters);
        redisNodes.addAll(slaves);

        List<String> downNodes = getDownNodeAddress(redisNodes);

        // 이전 크기 보다 작으면 다운된 것으로 간주
        if(redisNodes.size() < prevSize){
            prevSize = redisNodes.size();
            alertToSlck(downNodes);
        } else {
            prevSize = redisNodes.size();
        }

    }

    private List<String> getDownNodeAddress(Collection<RedisNode> redisNodes) {
        List<String> aliveNodes = new ArrayList<>();
        List<String> downNodes = new ArrayList<>();

        for (RedisNode redisNode : redisNodes) {
            InetSocketAddress addr = redisNode.getAddr();
            String ip = addr.getHostString();
            String port = String.valueOf(addr.getPort());
            String address = MessageFormat.format("{0}{1}:{2}",REDISSON_HOST_PREFIX, ip, port);
            aliveNodes.add(address);
        }

        for (String node : nodes) {
            if(!aliveNodes.contains(node)) {
                downNodes.add(node);
            }
        }

        return downNodes;
    }

    private void alertToSlck(List<String> nodeAddress) {
        slackClient.sendMessage(redisHealthCheckHookUrl, createMessage(nodeAddress));
    }

    private String createMessage(List<String> addr) {
        return MessageFormat.format("""
                ```
                ☠️ [레디스 노드 다운 알림]☠️
                📌 Node Address: {0}
                ⏳ Timestamp: {1}
                ```
                """, String.join(", ", addr), LocalDateTime.now().toString()
        );
    }
}
