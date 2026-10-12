package com.dducwsjvbe.web_ui.config;

import com.dducwsjvbe.web_ui.filter.TokenContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient() {
        // 1. Tạo factory để cấu hình Timeout cho kết nối HTTP
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        // Thời gian chờ kết nối tới Gateway (10 giây)
        requestFactory.setConnectTimeout(10_000);

        // Thời gian chờ Gateway/File-Service phản hồi data (30 giây - đủ cho các file/chunk dung lượng lớn)
        requestFactory.setReadTimeout(30_000);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String token = TokenContext.getAccessToken();
                    if (token != null) {
                        request.getHeaders().setBearerAuth(token);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

}
