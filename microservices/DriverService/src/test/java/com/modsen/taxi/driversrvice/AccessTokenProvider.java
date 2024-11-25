package com.modsen.taxi.driversrvice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@Service
public class AccessTokenProvider {

    private final WebTestClient webTestClient;
    private final String authServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public AccessTokenProvider(WebTestClient webTestClient, @Value("${keycloak.auth-server-url}") String authServerUrl) {
        this.webTestClient = webTestClient;
        this.authServerUrl = authServerUrl;
    }

    public String getAccessToken(String username, String password) {
        String requestBody = "username=" + username + "&" +
                "password=" + password + "&" +
                "grant_type=password&" +
                "scope=openid&" +
                "client_id=" + clientId + "&" +
                "client_secret=" + clientSecret;

        Map<String, Object> responseBody = webTestClient.post()
                .uri(authServerUrl + "/realms/taxiapp-realm/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        if (responseBody != null) {
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Failed to get access token");
        }
    }
}
