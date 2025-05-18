package com.example.simulators.settlement

import com.example.auction.model.AuctionId
import com.example.settlement.OrderId
import com.example.settlement.Settlement
import com.example.settlement.SettlementInstruction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.test.fail

@Controller
@RequestMapping("/settlement")
class SettlementController(@Autowired val service: SettlementSimulatorService) {
    companion object {
        val log = LoggerFactory.getLogger(SettlementController::class.java)
    }
    
    @PostMapping(produces = [], consumes = [APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun settle(@RequestBody settlement: SettlementInstruction) {
        log.info("Registering settlement")
        service.settle(settlement)
    }
    
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun get(@PathVariable orderId: OrderId): SettlementInstruction =
        service.get(orderId) ?: throw ResponseStatusException(NOT_FOUND)
}

interface SettlementTesting {
    operator fun get(orderId: OrderId): SettlementInstruction?
}

@Component
class SettlementSimulatorService : Settlement, SettlementTesting {
    private val lock = ReentrantReadWriteLock()
    private val orders = mutableMapOf<OrderId, Pair<SettlementInstruction, Int>>()
    
    override operator fun get(orderId: OrderId) =
        lock.read {
            orders[orderId]?.first
        }
    
    override fun settle(instruction: SettlementInstruction) {
        lock.write {
            val (prevInstruction, count) = orders.computeIfAbsent(instruction.order, { instruction to 0 })
            if (instruction != prevInstruction) {
                error("idempotency failure")
            } else {
                orders[instruction.order] = prevInstruction to count+1
            }
        }
    }
}

operator fun SettlementTesting.get(auction: AuctionId) =
    get(OrderId(auction))
        ?: fail("no settlement instruction generated for auction $auction")


@SpringBootApplication(
    scanBasePackageClasses = [SettlementSimulatorService::class],
    exclude = [DataSourceAutoConfiguration::class]
)
class SettlementSimulator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<SettlementSimulator>(*args)
        }
    }
}

