package com.wemadetest.accessanalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(new Info().title("Access Analyzer 로그 분석 API 서버")
                        .description("유저 접속 로그 관련 통계를 보여주는 API입니다.")
                        .version("0.0.1"))
                .servers(List.of(
                        new Server().url("http://localhost:8080")
                                .description("개발용 서버")
                ));
    }
}
