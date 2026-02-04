package com.wemadetest.accessanalyzer.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ip-info")
@Getter
@Setter
public class IpInfo {
    private String api;
    private String token;
}
