package com.wemadetest.accessanalyzer.service;

import com.wemadetest.accessanalyzer.dto.AnalysisDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class IpInfoService {
    private final WebClient webClient;

    public IpInfoService(WebClient webClient) {
        this.webClient = webClient;
    }


    @Cacheable(value = "ipInfoCache", key = "#ip")
    public AnalysisDto.IpInfoResponse getIpInfo(String ip){

        log.info("getIpInfo: ip={}", ip);

        return webClient.get()
                .uri("/{ip}", ip)
                .retrieve()
                .bodyToMono(AnalysisDto.IpInfoResponse.class)
                .block();
    }
}
