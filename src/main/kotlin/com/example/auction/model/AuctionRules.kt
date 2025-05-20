package com.example.auction.model

enum class AuctionRules(val decideWinner: (Auction) -> AuctionWinner?) {
    Blind(::blindAuctionWinner),
    Vickrey(::vickreyAuctionWinner),
    Reverse(::reverseAuctionWinner)
}
