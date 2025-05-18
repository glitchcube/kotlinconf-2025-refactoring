package com.example.auction.acceptance.http

import com.example.auction.AuctionApplication
import com.example.auction.acceptance.http.SpringHttpTesting.Config
import com.example.auction.model.AuctionId
import com.example.auction.service.AuctionSettlementService
import com.example.pii.UserId
import com.example.settlement.SettlementInstruction
import com.example.simulators.pii_vault.PiiVaultSimulator
import com.example.simulators.pii_vault.UserIdSetup
import com.example.simulators.settlement.SettlementSimulator
import com.example.simulators.settlement.SettlementTesting
import com.example.simulators.settlement.get
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner.Mode.OFF
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext
import org.springframework.boot.web.server.WebServer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.DynamicPropertyRegistrar
import java.net.URI
import java.util.UUID
import javax.sql.DataSource

@Tag("http")
@Tag("in-memory")
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = [AuctionApplication::class, Config::class],
    properties = [
        "spring.datasource.generate-unique-name=true"
    ]
)
abstract class SpringHttpTesting : HttpAuctionTesting(), RequiresDatabaseAccess {
    private val testId = UUID.randomUUID()
    
    @LocalServerPort
    private var port: Int = 0
    
    @Autowired
    override lateinit var objectMapper: ObjectMapper
    
    @Autowired
    override lateinit var dataSource: DataSource
    
    @Autowired
    lateinit var userIdSetup: UserIdSetup
    
    @Autowired
    lateinit var settlementTesting: SettlementTesting
    
    @Autowired
    lateinit var settlementService: AuctionSettlementService
    
    override val baseUri by lazy { URI("http://localhost:$port") }
    
    override fun aUser(name: String): UserId {
        val userId = UserId(name + "_" + testId)
        userIdSetup.addUserId(userId)
        return userId
    }
    
    override fun settlementOf(auction: AuctionId): SettlementInstruction {
        settlementService.requestSettlements()
        return settlementTesting[auction]
    }
    
    @Configuration
    class Config {
        companion object {
            val log: Logger = LoggerFactory.getLogger(SpringHttpTesting::class.java)
        }
        
        interface PiiVaultSimulatorRunner : AutoCloseable, WebServer
        
        @Bean
        fun piiVaultSimulator(): PiiVaultSimulatorRunner {
            val simulator = startSupportingApplication<PiiVaultSimulator>()
            return object : PiiVaultSimulatorRunner,
                UserIdSetup by simulator.getBean(UserIdSetup::class.java),
                AutoCloseable by simulator,
                WebServer by simulator.webServer {}
                .also { log.info("Starting PII Vault Simulator on port ${it.port}") }
        }
        
        @Bean
        fun piiVaultPropertiesRegistrar(runner: PiiVaultSimulatorRunner) =
            urlPropertyRegistrar("pii-vault.url", runner)
        
        interface SettlementSimulatorRunner : AutoCloseable, WebServer
        
        @Bean
        fun settlementSimulator(): SettlementSimulatorRunner {
            val simulator = startSupportingApplication<SettlementSimulator>()
            return object : SettlementSimulatorRunner,
                SettlementTesting by simulator.getBean(SettlementTesting::class.java),
                AutoCloseable by simulator,
                WebServer by simulator.webServer {}
                .also { log.info("Starting Settlement Simulator on port ${it.port}") }
        }
        
        @Bean
        fun settlementPropertiesRegistrar(runner: SettlementSimulatorRunner) =
            urlPropertyRegistrar("settlement.url", runner)
        
        private inline fun <reified T> startSupportingApplication() =
            SpringApplicationBuilder(T::class.java)
                .bannerMode(OFF)
                .properties("server.port=0")
                .web(SERVLET)
                .application()
                .run() as ConfigurableWebServerApplicationContext
        
        private fun urlPropertyRegistrar(propertyName: String, webServer: WebServer) =
            DynamicPropertyRegistrar {
                it.add(propertyName, { "http://localhost:${webServer.port}" })
            }
        
    }
}