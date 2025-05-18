package com.example.simulators.pii_vault

import com.example.pii.UserId
import com.example.pii.UserIdValidity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.concurrent.ConcurrentSkipListSet

@Controller
@RequestMapping("/users/{userId}")
class PiiVaultController(@Autowired val vaultService: PiiVaultSimulatorService) {
    companion object {
        val log = LoggerFactory.getLogger(PiiVaultController::class.java)
    }
    
    @PostMapping(produces = [], consumes = [])
    @ResponseStatus(HttpStatus.CREATED)
    fun insertUser(@PathVariable userId: UserId) {
        log.info("Creating user $userId")
        vaultService.addUserId(userId)
    }
    
    @GetMapping(produces = [APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    fun checkUser(@PathVariable userId: UserId) =
        UserIdValidity(vaultService.isValid(userId))
}

interface UserIdSetup {
    fun addUserId(userId: UserId)
}

@Component
class PiiVaultSimulatorService : UserIdSetup {
    private val userIds = ConcurrentSkipListSet(compareBy(UserId::value))
    
    override fun addUserId(userId: UserId) {
        check(userId !in userIds) { "a user with id $userId already exists" }
        userIds += userId
    }
    
    fun isValid(userId: UserId): Boolean {
        return userId in userIds
    }
}


@SpringBootApplication(
    scanBasePackageClasses = [PiiVaultSimulator::class],
    exclude = [DataSourceAutoConfiguration::class]
)
class PiiVaultSimulator {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<PiiVaultSimulator>(*args)
        }
    }
}
