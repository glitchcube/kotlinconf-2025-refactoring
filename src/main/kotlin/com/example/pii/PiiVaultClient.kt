package com.example.pii

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class PiiVaultClient(
    @Value("\${pii-vault.url}")
    val baseUrl: URI,
    val restClient: RestClient = RestClient.create()
) : UserIdValidator {
    override fun isValid(userId: UserId): Boolean {
        val response = restClient.get()
            .uri(UriComponentsBuilder.fromUri(baseUrl)
                .pathSegment("users", userId.value)
                .build()
                .toUri()
            )
            .retrieve()
            .body<UserIdValidity>()
        
        return response?.isValid ?: error("no content from PII Vault")
    }
}
