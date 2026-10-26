package com.example.pokedex.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableCaching
public class PokeApiConfig {

    @Bean
    RestClient pokeApiRestClient(RestClient.Builder builder,
                                 @Value("${app.pokeapi.base-url}") String baseUrl,
                                 @Value("${app.pokeapi.connect-timeout-ms}") int connectTimeout,
                                 @Value("${app.pokeapi.read-timeout-ms}") int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
