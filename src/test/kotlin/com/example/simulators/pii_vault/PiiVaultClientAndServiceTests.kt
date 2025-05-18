package com.example.simulators.pii_vault

import com.example.pii.PiiVaultClient
import com.example.pii.UserId
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnabledIfSystemProperty(named = "run-slow-tests", matches = "true")
@SpringBootTest(classes=[PiiVaultSimulator::class], webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class PiiVaultClientAndServiceTests {
    @LocalServerPort
    var port = 0
    
    @Autowired
    lateinit var service: PiiVaultSimulatorService
    
    val client by lazy { PiiVaultClient(URI("http://localhost:$port")) }
    
    @Test
    fun `invalid user`() {
        assertFalse(client.isValid(UserId("nobody")))
    }
    
    @Test
    fun `valid user`() {
        val userId = UserId("somebody")
        service.addUserId(userId)
        assertTrue(client.isValid(userId))
    }

    @Test
    fun `duplicate user`() {
        val userId = UserId("somebody")
        service.addUserId(userId)
        assertThrows<IllegalStateException> {
            service.addUserId(userId)
        }
    }
}
