package com.example.settlement

import com.example.auction.model.Money
import com.example.pii.UserId


data class SettlementInstruction(
    val order: OrderId,
    val collect: Collection,
    val charges: List<Charge>
)

data class Collection(
    val from: UserId,
    val to: UserId,
    val amount: Money
)

data class Charge(
    val fee: String,
    val from: UserId,
    val unit: Money,
    val quantity: Int,
    val total: Money
)

fun Charge(from: UserId, amount: Money, fee: String) =
    Charge(fee, from, unit = amount, quantity = 1, total = amount)

