package com.itmo.microservices.demo.stock.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.stock.api.event.AddItemToCatalogueEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemCreatedEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemDeletedEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemReservedEvent
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.api.service.StockItemService
import com.itmo.microservices.demo.stock.impl.logging.StockItemServiceNotableEvents
import com.itmo.microservices.demo.stock.impl.repository.StockItemRepository
import com.itmo.microservices.demo.stock.impl.util.toEntity
import com.itmo.microservices.demo.stock.impl.util.toModel
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Suppress("UnstableApiUsage")
@Service
class DefaultStockItemService(private val stockItemRepository: StockItemRepository,
                              private val eventBus: EventBus) : StockItemService{

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    override fun allStockItems(): List<StockItemModel> = stockItemRepository.findAll()
        .map { it.toModel() }

    override fun getStockItemById(stockItemId: UUID): StockItemModel =
        stockItemRepository.findByIdOrNull(stockItemId)?.toModel()
            ?: throw NotFoundException("Stock Item $stockItemId not found")

    //number can be negative
    override fun reserveStockItem(stockItemId: UUID, number: Int) : Boolean {
        val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return false
        val totalCount = stockItem.amount
        if (totalCount != null) {
            if (totalCount < number) {
                return false
            }
        }
                stockItem.setReservedCount(number)

        stockItemRepository.save(stockItem)
        eventBus.post(StockItemReservedEvent(stockItem.toModel()))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_RESERVED,
            stockItem
        )
        return true

    }

    override fun createStockItem(stockItem: StockItemModel) : StockItemModel? {
        val title = stockItem.title;
        if (title?.let { stockItemRepository.findByTitle(it) } == null) {
            stockItemRepository.save(stockItem.toEntity())
            eventBus.post(StockItemCreatedEvent(stockItem))
            //eventBus.post(AddItemToCatalogueEvent(stockItemEntity.toModel().id, stockItemEntity.toModel()))
            eventLogger.info(
                StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
                stockItem
            )
            return stockItem
        }
        else return null
    }

    override fun addStockItem(stockItemId: UUID, number: Int) {
            val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return
            stockItem.setAmount(number)
            stockItemRepository.save(stockItem)
            eventBus.post(StockItemCreatedEvent(stockItem.toModel()))
            eventLogger.info(
                StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
                stockItem
            )

    }

    override fun changeStockItem(stockItemId: UUID, stockItem: StockItemModel) {
        val stockItemFromDb = stockItemRepository.findByIdOrNull(stockItemId) ?: return
        stockItemFromDb.title = stockItem.title
        stockItemFromDb.description = stockItem.description
        stockItemFromDb.reservedCount = stockItem.reservedCount
        stockItemFromDb.amount = stockItem.amount
        stockItemFromDb.price = stockItem.price
        stockItemFromDb.category = stockItem.category
        stockItemRepository.save(stockItemFromDb)
        eventBus.post(StockItemCreatedEvent(stockItemFromDb.toModel()))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
            stockItem
        )
    }

    override fun deleteStockItemById(stockItemId: UUID) {
            val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return
            stockItemRepository.deleteById(stockItemId)
            eventBus.post(StockItemDeletedEvent(stockItem.toModel()))
            eventLogger.info(
                StockItemServiceNotableEvents.I_STOCK_ITEM_DELETED,
                stockItem
            )
    }

    override fun deductStockItem(stockItemId: UUID, number: Int) {
        val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return
        stockItem.setAmount(-number)
        stockItem.setReservedCount(-number)
        stockItemRepository.save(stockItem)
        eventBus.post(StockItemCreatedEvent(stockItem.toModel()))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
            stockItem
        )
    }
}
