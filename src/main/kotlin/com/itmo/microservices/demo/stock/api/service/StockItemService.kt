package com.itmo.microservices.demo.stock.api.service

import com.itmo.microservices.demo.stock.api.model.CatalogItemDto
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.impl.entity.StockItem
import java.util.*

interface StockItemService {
    fun allStockItems(available : Boolean): List<CatalogItemDto>
    fun createStockItem(stockItem: StockItemModel) : CatalogItemDto?
    fun getStockItemById(stockItemId: UUID) : StockItemModel
    fun addStockItem(stockItemId: UUID, number: Int)
    fun deleteStockItemById(stockItemId: UUID)
    fun changeStockItem(stockItemId: UUID, stockItem: StockItemModel)
    fun deductStockItem(stockItemId: UUID, number: Int) : Boolean
}
