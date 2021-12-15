package com.itmo.microservices.demo.stock.api.event

import com.itmo.microservices.demo.stock.api.model.StockItemModel
import java.util.*

data class AddItemToCatalogueEvent(val title: String?) {
}