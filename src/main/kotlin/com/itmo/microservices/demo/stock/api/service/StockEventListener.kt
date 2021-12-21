package com.itmo.microservices.demo.stock.api.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.stock.api.event.*
import com.itmo.microservices.demo.stock.api.model.BookingLogRecordModel
import com.itmo.microservices.demo.stock.impl.entity.BookingLogRecord
import com.itmo.microservices.demo.stock.impl.repository.BookingRepository
import com.itmo.microservices.demo.stock.impl.repository.StockItemRepository
import com.itmo.microservices.demo.stock.impl.util.toModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StockEventListener {
    @Autowired
    private lateinit var stockService : StockItemService

    @Autowired
    private lateinit var stockItemRepository : StockItemRepository

    @Autowired
    private lateinit var bookingRepository : BookingRepository

    @Autowired
    private lateinit var eventBus : EventBus

    @PostConstruct
    fun init(){
        eventBus.register(this)
    }

    @Subscribe
    fun onItemAddedToCatalog(event : AddItemToCatalogueEvent){
        println("Item " + event.title + " created.")
    }

    @Subscribe
    fun onItemDeleted(event : DeleteItemEvent){
        println("Item " + event.title + " deleted.")
    }

    @Subscribe
    fun onDeductItem(event : DeductItemEvent){
        val item = stockItemRepository.findByTitle(event.title!!)
        stockService.deductStockItem(item!!.id!!, event.number!!)
        println("Item " + event.title + " deducted by quantity " + event.number)
    }

    @Subscribe
    fun onAddItem(event : AddedItemEvent){
        println("Item " + event.title + " added by quantity " + event.number)
    }

    @Subscribe
    fun onBooking(event : BookingEvent){
       val booking = BookingLogRecord(null, event.bookingId, event.itemId, event.status,
       event.amount, event.timestamp)
        bookingRepository.save(booking)
    }
}