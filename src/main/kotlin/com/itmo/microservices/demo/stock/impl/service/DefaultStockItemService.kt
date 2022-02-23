package com.itmo.microservices.demo.stock.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.stock.api.event.*
import com.itmo.microservices.demo.stock.api.messaging.StockItemCreatedEvent
import com.itmo.microservices.demo.stock.api.messaging.StockItemDeletedEvent
import com.itmo.microservices.demo.stock.api.model.CatalogItemDto
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.api.service.StockItemService
import com.itmo.microservices.demo.stock.impl.logging.StockItemServiceNotableEvents
import com.itmo.microservices.demo.stock.impl.repository.StockItemRepository
import com.itmo.microservices.demo.stock.impl.util.toDto
import com.itmo.microservices.demo.stock.impl.util.toEntity
import com.itmo.microservices.demo.stock.impl.util.toModel
import io.prometheus.client.Counter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@Suppress("UnstableApiUsage")
@Service
class DefaultStockItemService(private val stockItemRepository: StockItemRepository,
                              private val eventBus: EventBus) : StockItemService{

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    val catalogShown: Counter = Counter.build()
        .name("catalog_shown").help("Catalog shown.")
        .labelNames("serviceName").register()

    override fun allStockItems(available : Boolean): List<CatalogItemDto> {
        catalogShown.labels("p04").inc();
        if(available) {
            return stockItemRepository.findAvailableItems().map { it.toDto() }
        }
        else return stockItemRepository.findUnavailableItems().map { it.toDto() }
    }

    override fun getStockItemById(stockItemId: UUID): StockItemModel =
        stockItemRepository.findByIdOrNull(stockItemId)?.toModel()
            ?: throw NotFoundException("Stock Item $stockItemId not found")


    override fun createStockItem(stockItem: StockItemModel) : CatalogItemDto? {
        if(stockItem.amount < 0){
            return null
        }
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

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "number < 0")
    class CannotSetNegativeCountException : RuntimeException()
    override fun addStockItem(stockItemId: UUID, number: Int) {
        if (number < 0)
            throw CannotSetNegativeCountException()
        val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return
        stockItem.addAmount(number)
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

    override fun deductStockItem(stockItemId: UUID, number: Int) : Boolean {
        if (number < 0 )
            throw CannotSetNegativeCountException()
        val stockItem = stockItemRepository.findByIdOrNull(stockItemId) ?: return false
        val totalCount = stockItem.amount
        if (totalCount != null) {
            if (totalCount < number!!) {
                return false
            }
        }
        stockItem.removeAmount(number)
        stockItemRepository.save(stockItem)
        eventBus.post(StockItemCreatedEvent(stockItem.toModel()))
        //eventBus.post(DeductItemEvent(stockItem.title, number))
        eventLogger.info(
            StockItemServiceNotableEvents.I_STOCK_ITEM_CHANGED,
            stockItem
        )
        return true
    }
}
