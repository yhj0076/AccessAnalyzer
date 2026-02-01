package com.wemadetest.accessanalyzer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class DetailLog {
    private LocalDateTime timeGeneratedUTC;
    private String clientIp;
    private String httpMethod;
    private String requestUri;
    private String userAgent;
    private HttpStatus httpStatus;
    private String httpVersion;
    private Integer receivedBytes;
    private Integer sentBytes;
    private Float clientResponseTime;
    private String sslProtocol;
    private String originalRequestUriWithArgs;
}