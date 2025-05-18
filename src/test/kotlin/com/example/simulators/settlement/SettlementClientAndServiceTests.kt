package com.example.simulators.settlement

import com.example.auction.acceptance.EUR
import com.example.pii.UserId
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementClient
import com.example.settlement.SettlementInstruction
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals

@EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
@SpringBootTest(
    classes = [SettlementSimulator::class],
    webEnvironment = RANDOM_PORT
)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class SettlementClientAndServiceTests {
    @LocalServerPort
    var port = 0
    
    @Autowired
    lateinit var service: SettlementSimulatorService
    
    val client by lazy { SettlementClient(URI("http://localhost:$port")) }
    
    @Test
    fun `sending settlement instruction`() {
        val sent = SettlementInstruction(
            order = OrderId("the-order-id"),
            collect = Collection(
                from = UserId("alice"),
                to = UserId("bob"),
                amount = 123.EUR
            ),
            charges = listOf(
                Charge(
                    fee = "commission",
                    from = UserId("alice"),
                    amount = 5.EUR,
                ),
                Charge(
                    fee = "bids",
                    from = UserId("bob"),
                    unit = 2.EUR,
                    quantity = 3,
                    total = 6.EUR
                )
            )
        )
        
        client.settle(sent)
        
        val received = service[sent.order]
        
        assertEquals(sent, received)
    }
}
