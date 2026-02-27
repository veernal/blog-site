package com.blogsite.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class SwaggerDocsAggregatorController {

    private final WebClient webClient;

    @Value("${user-service.url:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${blog-command-service.url:http://localhost:8082}")
    private String blogCommandServiceUrl;

    @Value("${blog-query-service.url:http://localhost:8083}")
    private String blogQueryServiceUrl;

    public SwaggerDocsAggregatorController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/swagger/{service}/v3/api-docs")
    public Mono<ResponseEntity<String>> getServiceSwagger(@PathVariable String service) {
        String url;
        switch (service) {
            case "user-service":
                url = userServiceUrl + "/v3/api-docs";
                break;
            case "blog-command-service":
                url = blogCommandServiceUrl + "/v3/api-docs";
                break;
            case "blog-query-service":
                url = blogQueryServiceUrl + "/v3/api-docs";
                break;
            default:
                return Mono.just(ResponseEntity.notFound().build());
        }
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }
}
