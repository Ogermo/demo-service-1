package com.itmo.microservices.demo.stock.api.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.stock.api.event.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StockEventListener {
    @Autowired
    private lateinit var stockService : StockItemService

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
    fun onReserveItem(event : ReserveItemEvent){
        stockService.reserveStockItem(event.id, event.number)
        println("Item " + event.id + " reserved by quantity " + event.number)
    }

    @Subscribe
    fun onDeductItem(event : DeductItemEvent){
        println("Item " + event.title + " deducted by quantity " + event.number)
    }

    @Subscribe
    fun onAddItem(event : AddedItemEvent){
        println("Item " + event.title + " added by quantity " + event.number)
    }
}