package com.itmo.microservices.demo.orders.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.orders.api.model.BookingDto
import com.itmo.microservices.demo.orders.api.model.OrderDto
import com.itmo.microservices.demo.orders.api.service.OrderService
import com.itmo.microservices.demo.orders.impl.entity.Order
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.impl.entity.OrderItems
import com.itmo.microservices.demo.orders.impl.logging.OrderServiceNotableEvents
import com.itmo.microservices.demo.orders.impl.repository.OrderItemsRepository
import com.itmo.microservices.demo.orders.impl.repository.OrderRepository
import com.itmo.microservices.demo.orders.impl.util.toBookingDto
import com.itmo.microservices.demo.orders.impl.util.toDto
import com.itmo.microservices.demo.orders.impl.util.toEntity
import com.itmo.microservices.demo.stock.api.event.BookingEvent
import com.itmo.microservices.demo.stock.api.event.DeductItemEvent
import com.itmo.microservices.demo.stock.api.model.BookingStatus
import com.itmo.microservices.demo.stock.api.service.StockItemService
import com.itmo.microservices.demo.stock.impl.repository.StockItemRepository
import com.itmo.microservices.demo.users.api.service.UserService
import javassist.NotFoundException
import kong.unirest.HttpStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Suppress("UnstableApiUsage")
@Service
class DefaultOrderService(private val orderRepository: OrderRepository,
                          private val orderItemsRepository: OrderItemsRepository,
                          private val stockItemRepository: StockItemRepository,
                          private val StockService: StockItemService,
                          private val eventBus: EventBus,
                          private val userService: UserService) : OrderService {

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

//    override fun getOrdersByUsername(user: UserDetails): List<OrderModel> {
//        val userId = getUserIdByUserDetails(user)
//        val orders = orderRepository.findAll()
//        val result = mutableListOf<OrderModel>()
//        for (order in orders) {
//            if(order.userId == userId) {
//                result.add(order.toModel())
//            }
//        }
//        return result
//    }
//
//    override fun getOrder(orderId: UUID): OrderModel {
//        return orderRepository.findByIdOrNull(orderId)?.toModel() ?: throw NotFoundException("Order $orderId not found")
//    }
//
    override fun book(orderId : UUID, user : UserDetails): BookingDto?{
        //CartService.booking(orderId);
        var order = orderRepository.findByIdOrNull(orderId) ?: return Order().toBookingDto(setOf())
        if(order.status != OrderStatus.COLLECTING){
            return null
        }
        var failedItems = mutableSetOf<UUID>()
        var itemsMap = orderItemsRepository.findByOrderId(orderId)
        for (item in itemsMap){
            var stockItem = stockItemRepository.findByIdOrNull(item.itemId)
            if (stockItem == null){

                failedItems.add(item.itemId!!)
            } else if (stockItem.amount!! < item.amount!!){
                failedItems.add(item.itemId!!)
            } else{

                var Am = stockItem.amount
                if (Am != null) {
                    eventBus.post(BookingEvent(order.id!!, item.id, BookingStatus.SUCCESS,
                        (item.amount)!!.toInt(), System.currentTimeMillis()))
                }
                else{

                    failedItems.add(item.itemId!!)
                }
            }
        }
        order.status = OrderStatus.BOOKED
        orderRepository.save(order)
        return order.toBookingDto(failedItems)
    }

    override fun deleteOrder(orderId: UUID) : Boolean{
        var order = orderRepository.findByIdOrNull(orderId) ?: throw NotFoundException("Order $orderId not found")
        if(order.status != OrderStatus.COLLECTING){
            return false
        }
        //eventBus.post(OrderDeletedEvent(order.toModel()))
        //eventLogger.info(OrderServiceNotableEvents.I_ORDER_DELETED, order)
        order.status = OrderStatus.DISCARD
        orderRepository.save(order)
        return true
    }
//
//    override fun assignPayment(orderId: UUID, payment : PaymentModel) {
//        var order = orderRepository.findByIdOrNull(orderId) ?: throw NotFoundException("Order $orderId not found")
//        if(order.status != 0)
//            throw OperationNotSupportedException("Order has already been paid")
//        order.status = 1
//        orderRepository.save(order)
//        val paymentEntity = payment.toEntity()
//        eventBus.post(PaymentAssignedEvent(payment))
//        eventLogger.info(OrderServiceNotableEvents.I_PAYMENT_ASSIGNED, paymentEntity)
//        paymentRepository.save(paymentEntity)
//    }
//
//    fun getUserIdByUserDetails(user : UserDetails) : UUID {
//        return UUID.fromString("0-0-0-0-0")
//    }
    override fun createOrder(userId: UUID) : OrderDto {

        val newOrder = Order(null, System.currentTimeMillis(), OrderStatus.COLLECTING, userId)

        orderRepository.save(newOrder)
        //CartService.makeCart(newOrder.id)
        return newOrder.toDto(mapOf())
    }

    override fun putItemToOrder(orderId : UUID, itemId : UUID, amount : Int): ResponseEntity<Nothing> {
        eventLogger.info(OrderServiceNotableEvents.I_ORDER_ADDED, listOf(orderId,itemId,amount))
        val item = stockItemRepository.findByIdOrNull(itemId)
        if(item == null || item.amount!! < amount){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
        val order = orderRepository.findByIdOrNull(orderId) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        if(order.status != OrderStatus.COLLECTING){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
        var itemList = orderItemsRepository.findByOrderId(orderId)
        for (x in itemList){
            if(x.itemId!!.equals(itemId)){
                var currentAmount = x.amount ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
                if (currentAmount + amount < 0){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
                }
                orderItemsRepository.save(OrderItems(x.id,orderId,itemId,currentAmount + amount))
                //CartService.putItemInCart(orderId, itemId, amount)
                return ResponseEntity.status(HttpStatus.OK).body(null)
            }
        }
        orderItemsRepository.save(OrderItems(null,orderId,itemId,amount))
        return ResponseEntity.status(HttpStatus.OK).body(null)

    //        var order = orderRepository.findByIdOrNull(orderId) ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
//        if (itemId in order.itemsMap.keys){
//            var currentAmount = order.itemsMap[itemId] ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
//            order.itemsMap[itemId] = amount + currentAmount
//        }
//        else{
//            order.itemsMap[itemId] = amount
//        }
//        orderRepository.save(order)
//        CartService.putItemInCart(orderId, itemId, amount)
//        return ResponseEntity.status(HttpStatus.OK).body(null)
    }

    override fun getOrder(orderId: UUID): OrderDto {
        val order = orderRepository.findByIdOrNull(orderId) ?: return Order().toDto(mapOf())
        return order.toDto(orderItemsRepository.findByOrderId(orderId).map{it.itemId!! to it.amount!!}.toMap())
    }

    override fun requestDeductStockItems(orderId: UUID) {

        val orderDto = getOrder(orderId)
        orderDto.itemsMap.forEach {
            val item = stockItemRepository.findByIdOrNull(it.key)
            val amount = it.value.toInt()

            eventBus.post(DeductItemEvent(item!!.title, amount))
        }
    }

    override fun changeOrderStatus(orderId: UUID, status: OrderStatus) {

        val orderDto = getOrder(orderId)

        orderDto.status = OrderStatus.PAID

        val order = orderRepository.findByIdOrNull(orderId)

        orderRepository.save(orderDto.toEntity(order!!.userId))
    }
}