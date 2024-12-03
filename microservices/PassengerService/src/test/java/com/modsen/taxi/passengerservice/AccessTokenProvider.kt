package com.modsen.taxi.passengerservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.test.web.reactive.server.WebTestClient

@Service
class AccessTokenProvider(
    private val webTestClient: WebTestClient,
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
) {

    @Value("\${keycloak.client-id}")
    private lateinit var clientId: String

    @Value("\${keycloak.client-secret}")
    private lateinit var clientSecret: String

    fun getAccessToken(username: String, password: String): String {
        val requestBody = mapOf(
            "username" to username,
            "password" to password,
            "grant_type" to "password",
            "scope" to "openid",
            "client_id" to clientId,
            "client_secret" to clientSecret
        ).map { "${it.key}=${it.value}" }
            .joinToString("&")

        val responseBody = webTestClient.post()
            .uri("$authServerUrl/realms/taxiapp-realm/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isOk
            .expectBody(Map::class.java)
            .returnResult()
            .responseBody

        return (responseBody?.get("access_token") as? String)
            ?: throw RuntimeException("Failed to get access token")
    }
}
