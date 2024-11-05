package com.projectw.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username}")
    private String elasticsearchUsername;

    @Value("${spring.elasticsearch.password}")
    private String elasticsearchPassword;

    @Value("${spring.elasticsearch.encoded.api.key}")
    private String encodedApiKey;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 인증 정보 제공을 위한 CredentialsProvider 설정
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticsearchUsername, elasticsearchPassword));
        try {
            RestClient restClient = RestClient
                    .builder(HttpHost.create(elasticsearchUrl))
                    .setDefaultHeaders(new Header[]{
                            new BasicHeader("Authroization", "ApiKey " + encodedApiKey),
                    })
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider)
                    ).build();

            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

            return new ElasticsearchClient(transport);
        } catch (Exception e) {
            log.error("Elasticsearch rest client error: {}", e);
            throw new RuntimeException(e);
        }
    }
}
