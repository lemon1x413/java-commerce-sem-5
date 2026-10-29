package com.example.spacecatsmarket.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CosmicInfoClient {

    private final RestClient restClient;

    public CosmicInfoClient(
            @Value("${cosmic-info.base-url:http://localhost:9090}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String getCosmicFact(String keyword) {
        return restClient.get()
                .uri("/api/v1/facts?keyword={keyword}", keyword)
                .retrieve()
                .body(String.class);
    }
}
