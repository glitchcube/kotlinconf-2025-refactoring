package com.example.settlement

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class SettlementClient(
    @Value("\${settlement.url}")
    val baseUrl: URI,
    val restClient: RestClient = RestClient.create()
) : Settlement {
    
    override fun settle(instruction: SettlementInstruction) {
        restClient.post()
            .uri(
                UriComponentsBuilder.fromUri(baseUrl)
                    .pathSegment("settlement")
                    .build()
                    .toUri()
            )
            .contentType(APPLICATION_JSON)
            .body(instruction)
            .retrieve()
            .toBodilessEntity()
    }
}
