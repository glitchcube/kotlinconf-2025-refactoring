@file:Suppress("ClassName")
package com.example.auction.acceptance.mem

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

class DomainModel_BlindAuction_BiddingAndWinning_Tests :
    DomainModelOnlyTesting(), BlindAuction_BiddingAndWinning_Tests
class DomainModel_VickreyAuction_BiddingAndWinning_Tests :
    DomainModelOnlyTesting(), VickreyAuction_BiddingAndWinning_Tests
class DomainModel_ReverseAuction_BiddingAndWinning_Tests :
    DomainModelOnlyTesting(), ReverseAuction_BiddingAndWinning_Tests
class DomainModel_ErrorReporting_Tests :
    DomainModelOnlyTesting(), ErrorReportingTests
class DomainModel_ListingAuctions_Tests :
    DomainModelOnlyTesting(), ListingAuctionsTests
class DomainModel_UserVerificationTests :
    DomainModelOnlyTesting(), UserVerificationTests
class DomainModel_SealedBidAuction_Charges_Tests :
    DomainModelOnlyTesting(), SealedBidAuction_Charges_Tests
class DomainModel_ReverseAuction_Charges_Tests :
    DomainModelOnlyTesting(), ReverseAuction_Charges_Tests
class DomainModel_RoundingCharges_Tests :
    DomainModelOnlyTesting(), RoundingChargesTests
class DomainModel_ReservePrice_Tests :
    DomainModelOnlyTesting(), ReservePriceTests
