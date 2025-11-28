package com.example.demo.services;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserInfoResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.reactive.function.client.WebClient;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InternalApiClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = "http://localhost:8080";
    private final String username = "maingoctu";
    private final String password = "123456";

    public UserInfoResponse getUserInfo(String userId) {
            String url = baseUrl + "/internal" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password); // ← Tự động encode
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<ApiResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ApiResponse.class
            );

            return (UserInfoResponse) response.getBody().getResult();
        }
    }

