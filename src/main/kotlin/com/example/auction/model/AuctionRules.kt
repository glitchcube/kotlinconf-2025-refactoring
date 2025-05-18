package com.example.auction.model

enum class AuctionRules {
    Blind {
        override fun decideWinner(auction: Auction): AuctionWinner? =
            auction.bids
                .associateBy { it.buyer }
                .values
                .maxByOrNull { it.amount }
                ?.takeIf { it.amount >= auction.reserve }?.toWinner()
    },
    Vickrey {
        override fun decideWinner(auction: Auction): AuctionWinner? =
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
    },
    Reverse {
        override fun decideWinner(auction: Auction): AuctionWinner? =
            auction.bids
                .filter { it.amount >= auction.reserve }
                .groupBy { it.amount }
                .values
                .filter { it.size == 1 }
                .map { it.single() }
                .minByOrNull { it.amount }?.toWinner()
    };

    abstract fun decideWinner(auction: Auction): AuctionWinner?
}