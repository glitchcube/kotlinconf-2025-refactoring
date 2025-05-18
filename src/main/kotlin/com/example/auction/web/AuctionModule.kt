package com.example.auction.web

import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
class AuctionModule() : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        addSerializer(object : StdSerializer<MonetaryAmount>(MonetaryAmount::class.java) {
            override fun serialize(value: MonetaryAmount, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeNumber(value.repr)
            }
        })
        addDeserializer(
            MonetaryAmount::class.java,
            object : StdDeserializer<MonetaryAmount>(MonetaryAmount::class.java) {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
                    p.decimalValue?.let { MonetaryAmount(it) }
            })
        
        addSerializer(object : StdSerializer<Money>(Money::class.java) {
            override fun serialize(value: Money, gen: JsonGenerator, provider: SerializerProvider) {
                gen.writeString(value.toString())
            }
        })
        addDeserializer(Money::class.java, object : StdDeserializer<Money>(Money::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext) =
                p.valueAsString?.let { Money.parse(it) }
                    ?: throw JsonParseException("invalid Money value: ${p.valueAsString}")
        })
        
        super.setupModule(context)
    }
}