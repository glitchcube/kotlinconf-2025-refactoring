package com.example.auction.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NON_TEST
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE)
@AutoConfigureTestDatabase(replace = NON_TEST)
class SpringJdbcAuctionRepositoryTest : AuctionRepositoryContract {
    @Autowired
    override lateinit var repository: SpringJdbcAuctionRepository
}