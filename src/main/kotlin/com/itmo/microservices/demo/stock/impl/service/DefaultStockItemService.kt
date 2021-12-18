package com.itmo.microservices.demo.stock.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.stock.api.event.*
import com.itmo.microservices.demo.stock.api.messaging.StockItemCreatedEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemDeletedEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemReservedEvent
import com.itmo.microservices.demo.stock.api.model.CatalogItemDto
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.api.service.StockItemService
import com.itmo.microservices.demo.stock.impl.logging.StockItemServiceNotableEvents
import com.itmo.microservices.demo.stock.impl.repository.StockItemRepository
import com.itmo.microservices.demo.stock.impl.util.toDto
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

    override fun allStockItems(): List<CatalogItemDto> = stockItemRepository.findAll()
        .map { it.toDto() }

    override fun getStockItemById(stockItemId: UUID): StockItemModel =
        stockItemRepository.findByIdOrNull(stockItemId)?.toModel()
            ?: throw NotFoundException("Stock Item $stockItemId not found")

    //number can be negative
    override fun reserveStockItem(stockItemId: UUID?, number: Int?) : Boolean {
        val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return false
        val totalCount = stockItem.amount
        if (totalCount != null) {
            if (totalCount < number!!) {
                return false
            }
        }
                stockItem.setReservedCount(number!!)

        stockItemRepository.save(stockItem)
        eventBus.post(StockItemReservedEvent(stockItem.toModel()))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_RESERVED,
            stockItem
        )
        return true

    }

    override fun createStockItem(stockItem: StockItemModel) : CatalogItemDto? {
        val title = stockItem.title;
        if (title?.let { stockItemRepository.findByTitle(it) } == null) {
            val entity  = stockItem.toEntity()
            stockItemRepository.save(entity)
            eventBus.post(StockItemCreatedEvent(stockItem))
            eventBus.post(AddItemToCatalogueEvent(title))
            eventLogger.info(
                StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
                stockItem
            )
            return entity.toDto()
        }
        else return null
    }

    override fun addStockItem(stockItemId: UUID, number: Int) {
            val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return
            stockItem.setAmount(number)
            stockItemRepository.save(stockItem)
            eventBus.post(StockItemCreatedEvent(stockItem.toModel()))
            eventBus.post(AddedItemEvent(stockItem.title, number))
            eventLogger.info(
                StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
                stockItem
            )

    }

    override fun changeStockItem(stockItemId: UUID, stockItem: StockItemModel) {
        val stockItemFromDb = stockItemRepository.findByIdOrNull(stockItemId) ?: return
        stockItemFromDb.title = stockItem.title
        stockItemFromDb.description = stockItem.description
        //stockItemFromDb.reservedCount = stockItem.reservedCount
        stockItemFromDb.amount = stockItem.amount
        stockItemFromDb.price = stockItem.price
        //stockItemFromDb.category = stockItem.category
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
        eventBus.post(DeleteItemEvent(stockItem.title))
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
        eventBus.post(DeductItemEvent(stockItem.title, number))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
            stockItem
        )
    }
}
