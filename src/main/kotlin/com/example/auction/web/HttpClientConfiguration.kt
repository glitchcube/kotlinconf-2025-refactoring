package com.example.auction.web

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class HttpClientConfiguration {
    companion object {
        val log: Logger = LoggerFactory.getLogger(HttpClientConfiguration::class.java)
    }
    
    @Bean
    fun restClient() =
        RestClient.builder()
            .requestInterceptor { request, body, execution ->
                execution.execute(request, body)
                    .also { response ->
                        log.info(
                            "Outgoing request received ${response.statusCode} from ${request.method} ${request.uri}"
                        )
                    }
            }
            .build()
}