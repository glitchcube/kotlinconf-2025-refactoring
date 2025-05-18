package com.example.settlement

interface Settlement {
    fun settle(instruction: SettlementInstruction)
}