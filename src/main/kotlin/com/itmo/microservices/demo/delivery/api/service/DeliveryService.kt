package com.itmo.microservices.demo.delivery.api.service

import com.itmo.microservices.demo.delivery.api.model.DeliveryModel
import com.itmo.microservices.demo.orders.api.model.BookingDto
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import kong.unirest.json.JSONObject
import java.util.*

interface DeliveryService {
    fun getDelivery(deliveryId : UUID) : DeliveryModel
    fun getDeliveryByOrder(orderId : UUID) : List<DeliveryModel>
    fun addDelivery(delivery: DeliveryModel)
    fun deleteDelivery(deliveryId: UUID)
    fun finalizeDelivery(deliveryId : UUID)
    fun getDeliverySlots(number: Int): List<Int>
    fun reserveDeliverySlots(orderId: UUID, slotInSec: Int) : BookingDto
    fun getDeliveryBySlot(slot: Int) : DeliveryModel
}