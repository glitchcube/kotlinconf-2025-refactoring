package com.example.auction.model

enum class AuctionRules {
    Blind {
        override fun decideWinner(auction: Auction): AuctionWinner? {
            TODO("Not yet implemented")
        }
    }, Vickrey {
        override fun decideWinner(auction: Auction): AuctionWinner? {
            return vickreyActionWinner(auction)
        }
    }, Reverse {
        override fun decideWinner(auction: Auction): AuctionWinner? {
            TODO("Not yet implemented")
        }
    };

    abstract fun decideWinner(auction: Auction): AuctionWinner?
}

fun vickreyActionWinner(auction: Auction): AuctionWinner? =
    auction.bids
        .associateBy { it.buyer }
        .values
        .sortedByDescending { it.amount }
        .take(2)
        .run {
            when {
                isEmpty() -> null
                last().amount < auction.reserve -> null
                else -> AuctionWinner(first().buyer, last().amount)
            }
        }