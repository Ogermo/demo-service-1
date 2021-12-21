package com.itmo.microservices.demo.stock.api.model

import java.util.*

data class CatalogItemDto(
    val id: UUID?,
    val title: String,
    val description: String,
    val price: Int,
    val amount: Int
)
