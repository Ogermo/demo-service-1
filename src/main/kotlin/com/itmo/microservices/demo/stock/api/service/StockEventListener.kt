package com.itmo.microservices.demo.stock.api.service

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.itmo.microservices.demo.stock.api.event.AddItemToCatalogueEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class StockEventListener {
    /*@Autowired
    private lateinit var stockService : StockItemService*/

    @Autowired
    private lateinit var eventBus : EventBus

    @PostConstruct
    fun init(){
        eventBus.register(this)
    }

    @Subscribe
    fun onItemAddedToCatalog(event : AddItemToCatalogueEvent){
        System.out.println("Item " + event.itemId + "with name " + event.item.title + " created.")
    }
}