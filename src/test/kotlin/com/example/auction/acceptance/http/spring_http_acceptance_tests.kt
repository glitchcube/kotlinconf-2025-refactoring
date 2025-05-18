@file:Suppress("ClassName")
package com.example.auction.acceptance.http

import com.example.auction.acceptance.BlindAuction_BiddingAndWinning_Tests
import com.example.auction.acceptance.ErrorReportingTests
import com.example.auction.acceptance.ListingAuctionsTests
import com.example.auction.acceptance.ReservePriceTests
import com.example.auction.acceptance.ReverseAuction_BiddingAndWinning_Tests
import com.example.auction.acceptance.ReverseAuction_Charges_Tests
import com.example.auction.acceptance.RoundingChargesTests
import com.example.auction.acceptance.SealedBidAuction_Charges_Tests
import com.example.auction.acceptance.UserVerificationTests
import com.example.auction.acceptance.VickreyAuction_BiddingAndWinning_Tests

class Http_BlindAuction_BiddingAndWinning_Tests : SpringHttpTesting(), BlindAuction_BiddingAndWinning_Tests
class Http_VickreyAuction_BiddingAndWinning_Tests : SpringHttpTesting(), VickreyAuction_BiddingAndWinning_Tests
class Http_ReverseAuction_BiddingAndWinning_Tests : SpringHttpTesting(), ReverseAuction_BiddingAndWinning_Tests
class Http_ErrorReportingTests : SpringHttpTesting(), ErrorReportingTests
class Http_ListingAuctionsTests : SpringHttpTesting(), ListingAuctionsTests
class Http_UserVerificationTests : SpringHttpTesting(), UserVerificationTests
class Http_SealedBidAuction_Charges_Tests : SpringHttpTesting(), SealedBidAuction_Charges_Tests
class Http_ReverseAuction_Charges_Tests : SpringHttpTesting(), ReverseAuction_Charges_Tests
class Http_ConcurrenctBidding_Tests : SpringHttpTesting(), ConcurrentBiddingTests
class Http_ConcurrenctBiddingAndClosing_Tests : SpringHttpTesting(), ConcurrentBiddingAndClosingTests
class Http_RoundingCharges_Tests : SpringHttpTesting(), RoundingChargesTests
class Http_ReservePrice_Tests : SpringHttpTesting(), ReservePriceTests
