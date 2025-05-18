package com.example.auction

import com.example.pii.PiiVaultClient
import com.example.settlement.SettlementClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackageClasses = [
        AuctionApplicationConfiguration::class,
        PiiVaultClient::class,
        SettlementClient::class
    ],
)
class AuctionApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<AuctionApplication>(*args)
        }
    }
}

