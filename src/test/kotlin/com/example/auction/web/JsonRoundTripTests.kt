package com.example.auction.web

import com.example.auction.EUR
import com.example.auction.GBP
import com.example.auction.acceptance.EUR
import com.example.auction.model.AuctionId
import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.MonetaryAmount
import com.example.auction.percent
import com.example.auction.service.AuctionResult
import com.example.auction.service.AuctionSummary
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.CreateAuctionResponse
import com.example.auction.service.CreateBlindAuctionRequest
import com.example.auction.service.CreateReverseAuctionRequest
import com.example.auction.service.CreateVickreyAuctionRequest
import com.example.auction.service.Passed
import com.example.auction.service.Sold
import com.example.pii.UserId
import com.example.pii.UserIdValidity
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
import com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.oneeyedmen.okeydoke.Approver
import com.oneeyedmen.okeydoke.Reporters
import com.oneeyedmen.okeydoke.sources.FileSystemSourceOfApproval
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import java.io.File
import java.util.TreeMap
import kotlin.reflect.KClass
import kotlin.test.assertEquals


@JsonTest
class JsonRoundTripTests {
    data class Sample(
        val sample: Any,
        val toClass: KClass<*>,
        val name: String,
        val assertEqual: (Any, Any) -> Unit,
    )
    
    @Autowired
    lateinit var jsonMapper: ObjectMapper
    
    val stableMapper = JsonMapper.builder()
        .enable(INDENT_OUTPUT)
        .enable(SORT_PROPERTIES_ALPHABETICALLY)
        .enable(ORDER_MAP_ENTRIES_BY_KEYS)
        .nodeFactory(object : JsonNodeFactory() {
            override fun objectNode() = ObjectNode(this, TreeMap())
        })
        .build()
    
    @TestFactory
    fun `json serde`(): List<DynamicNode> {
        return samples.map { (original, targetType, name, assertion) ->
            dynamicContainer(
                name, listOf(
                    dynamicTest("$name -> ${targetType.simpleName}") {
                        checkRoundTrip(original, targetType, assertion)
                    },
                    dynamicTest("$name serialised") {
                        approveJsonFormat(name, original)
                    }
                ))
        }
    }
    
    private fun checkRoundTrip(original: Any, targetType: KClass<*>, assertion: (Any, Any) -> Unit) {
        val json = jsonMapper.writeValueAsString(original)
        val deserialized = jsonMapper.readValue(json, targetType.java)
        assertion(original, deserialized)
    }
    
    private fun approveJsonFormat(name: String, original: Any) {
        Approver(name, sourceOfApproval)
            .assertApproved(
                stableMapper.writeValueAsString(
                    stableMapper.readTree(
                        jsonMapper.writeValueAsString(original)
                    )
                )
            )
    }
    
    companion object {
        val samples = listOf(
            BidRequest(
                buyer = UserId("the-buyer"),
                amount = com.example.auction.model.Money(MonetaryAmount("456.78"), EUR)
            ).asSample(),
            
            CreateBlindAuctionRequest(
                seller = UserId("the-user"),
                description = "a product",
                reserve = com.example.auction.model.Money(MonetaryAmount("456.78"), EUR),
                commission = 21.percent
            ).asSample<CreateAuctionRequest>(),
            
            CreateVickreyAuctionRequest(
                seller = UserId("the-user"),
                description = "a product",
                reserve = com.example.auction.model.Money(MonetaryAmount("456.78"), EUR),
                commission = 13.percent
            ).asSample<CreateAuctionRequest>(),
            
            CreateReverseAuctionRequest(
                seller = UserId("the-user"),
                description = "a product",
                reserve = 0.EUR,
                chargePerBid = "3.50".EUR
            ).asSample<CreateAuctionRequest>(),
            
            CreateAuctionResponse(
                auctionId = AuctionId(123L)
            ).asSample(),
            
            Passed.asSample<AuctionResult>(),
            
            Sold(
                UserId("the-winner"),
                com.example.auction.model.Money(MonetaryAmount("910.11"), GBP)
            ).asSample<AuctionResult>(),
            
            UserIdValidity(true).asSample("UserIdValidity(true)"),
            UserIdValidity(false).asSample("UserIdValidity(false)"),
            
            AuctionSummary(
                id = AuctionId(123L),
                rules = Reverse,
                seller = UserId("the-seller"),
                description = "a product",
                currency = GBP,
                reserve = 100.EUR,
                commission = 23.percent,
                chargePerBid = 3.EUR
            ).asSample("AuctionSummary"),
            
            
            SettlementInstruction(
                order = OrderId(AuctionId(12345)),
                collect = Collection(
                    from = UserId("wendy"),
                    to = UserId("simon"),
                    amount = 100.EUR
                ),
                charges = listOf(
                    Charge(
                        fee = "commission",
                        from = UserId("simon"),
                        unit = 10.EUR,
                        quantity = 1,
                        total = 10.EUR
                    )
                )
            ).asSample("Blind Auction settlement"),
            
            SettlementInstruction(
                order = OrderId(AuctionId(9123)),
                collect = Collection(
                    from = UserId("william"),
                    to = UserId("sally"),
                    amount = 17.EUR,
                ),
                charges = listOf(
                    Charge(
                        fee = "bids",
                        from = UserId("william"),
                        unit = 1.EUR,
                        quantity = 5,
                        total = 5.EUR
                    ),
                    Charge(
                        fee = "bids",
                        from = UserId("brenda"),
                        unit = 1.EUR,
                        quantity = 3,
                        total = 3.EUR
                    ),
                    Charge(
                        fee = "bids",
                        from = UserId("bob"),
                        unit = 1.EUR,
                        quantity = 4,
                        total = 4.EUR
                    )
                )
            ).asSample("Reverse auction settlement")
        )
        
        private val jClass = JsonRoundTripTests::class.java
        private val approvalDir = File("src/test/resources/${jClass.`package`.name.replace('.', '/')}/json")
        private val sourceOfApproval = FileSystemSourceOfApproval(
            approvalDir, approvalDir, ".json",
            Reporters.fileSystemReporter()
        )
        
        private inline fun <reified T : Any> T.asSample(
            name: String = this::class.simpleName ?: "UNKNOWN",
            noinline assertEqual: (Any, Any) -> Unit = { a, b -> assertEquals(a, b) },
        ) = Sample(
            sample = this,
            toClass = T::class,
            name = name,
            assertEqual = assertEqual
        )
    }
}

