# Auction Service refactoring example

## Introduction

A Spring Boot application that implements auctions.

This application exhibits several aspects of common applications:

* Domain model with polymorphic objects
* HTTP & JSON APIs
* Database persistence
* Multi-service architecture
* Synchronous communication between services.
* Asynchronous communication between services via a variant of the Transactional Outbox pattern.
* Functional and unit tests

Unlike many example Spring app(s), this app:

* implements business logic, not just CRUD
* performs autonomous activity
* implements database transactions
* *tests* the database transactions


## Simplifications

The service is part of a larger architecture (which conveniently simplifies the code and makes it practical for the limited time of this workshop).

* Communication between services is secured by a service mesh, so the services themselves do not need to authenticate and authorise incoming requests.
* Users can both create auctions and bid in auctions, so there is no need for users to have different roles.
* Personal Identifiable Information (PII) about our users is stored in a data vault service, and other services refer to users only by opaque IDs.  The PII vault service allows authorised services to fetch only the PII that they require for their functionality.  It also provides an API for any service to check whether a user ID is valid without exposing any personal information. The Auction service does not need any PII, and only needs to check that user IDs are valid.
* A Settlement service tracks payments owed to us by users, and that we owe to users. The Auction service registers payments with the Settlement service asynchronously, by using the Transactional Outbox pattern.

```plantuml
component browser as "Web Browser"
component bff as "BFF"
component auctions as "Auctions"
component pii as "PII Vault"
component settlement as "Settlement"
actor user
cloud payment as "Payment Systems"

user --> browser
browser --> bff
bff -> auctions
bff --> pii
auctions --> pii
settlement --> pii
auctions <-> settlement 
settlement <-> payment
```

## Domain Model

```plantuml
abstract class Auction {
    seller: UserId
    description: String
    currency: Currency
    reserve: Money
    commission: BigDecimal
    chargePerBid: Money
    status: {open,closed,settling,settled}
    
    close(BidQueries): AuctionWinner?
    # {abstract} decideWinner(BidQueries): AuctionWinner?
}

class BlindAuction extends Auction {
    # decideWinner(...)
}
class VickreyAuction extends Auction {
    # decideWinner(...)
}
class ReverseAuction extends Auction {
    # decideWinner(...)
}

class AuctionWinner {
    bidder: UserId
    owed: Money
}
Auction "1" o-> "0..1" AuctionWinner 

interface BidQueries

Auction --> BidQueries : uses

class Bid 
BidQueries .> "*" Bid : returns

interface AuctionRepository extends BidQueries

class InMemoryAuctionRepository implements AuctionRepository

class JdbcAuctionRepository implements AuctionRepository
```

The service currently implements three different types of auction:

- **Blind auctions** (also known as Sealed Bid First Price auctions). The bidder with the highest bid wins the auction. In case of a tie, the earliest of the highest bidders wins. The seller is charged commission as a percentage of the final sale price.

- **Vickrey auctions** (also known as Sealed Bid Second Price auctions, used by stamp collectors).  The bidder with the highest bid wins, but pays the bid of the second-highest bidder.  In case of a tie, the earliest highest-bidder wins and pays the bid of the second-earliest highest-bidder, which will be the same amount as the winning bid. The seller is charged commission as a percentage of the final sale price.

- **Reverse auctions**. Bidders can submit multiple bids. The lowest unique bid wins. The bidders are charged a fixed fee per bid.

When an auction is open it has no winner. When an auction is closed, the application determines the winner and records the decision as an AuctionWinner object linked to the Auction.
