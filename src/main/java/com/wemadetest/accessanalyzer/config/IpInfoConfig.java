package com.wemadetest.accessanalyzer.config;

import com.wemadetest.accessanalyzer.entity.IpInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Configuration
public class IpInfoConfig {
    private final IpInfo ipInfo;
    public IpInfoConfig(IpInfo ipInfo) {
        this.ipInfo = ipInfo;
    }

    @Bean
    public WebClient ipInfoClient(IpInfo ipInfo) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3));

        return WebClient.builder()
                .baseUrl(ipInfo.getApi())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> {

                    URI newUri = UriComponentsBuilder
                            .fromUri(request.url())
                            .queryParam("token", ipInfo.getToken())
                            .build(true)
                            .toUri();

                    ClientRequest newRequest =
                            ClientRequest.from(request)
                                    .url(newUri)
                                    .build();

                    return next.exchange(newRequest);
                })
                .build();
    }

}
