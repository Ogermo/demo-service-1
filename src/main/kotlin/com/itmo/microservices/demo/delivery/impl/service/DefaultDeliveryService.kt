package com.itmo.microservices.demo.delivery.impl.service


import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.common.exception.NotFoundException
import com.itmo.microservices.demo.delivery.DeliveryExternalService.ExecutorsFactory
import com.itmo.microservices.demo.delivery.api.messaging.DeliveryCreatedEvent
import com.itmo.microservices.demo.delivery.api.messaging.DeliveryDeletedEvent
import com.itmo.microservices.demo.delivery.api.model.DeliveryModel
import com.itmo.microservices.demo.delivery.api.model.Status
import com.itmo.microservices.demo.delivery.api.service.DeliveryService
import com.itmo.microservices.demo.delivery.impl.entity.Delivery
import com.itmo.microservices.demo.delivery.impl.logging.DeliveryServiceNotableEvents
import com.itmo.microservices.demo.delivery.impl.repository.DeliveryRepository
import com.itmo.microservices.demo.delivery.impl.util.toEntity
import com.itmo.microservices.demo.delivery.impl.util.toModel
import com.itmo.microservices.demo.orders.api.event.SlotReserveReponseEvent
import com.itmo.microservices.demo.orders.api.model.BookingDto
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.impl.entity.Order
import com.itmo.microservices.demo.orders.impl.repository.OrderRepository
import com.itmo.microservices.demo.orders.impl.util.toBookingDto
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutionException

@Service
class DefaultDeliveryService(private val deliveryRepository: DeliveryRepository,
                             private val eventBus: EventBus,
                             private val orderRepository: OrderRepository,  private val meterRegistry: MeterRegistry) : DeliveryService {

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    private var httpClient : HttpClient = HttpClient.newBuilder().executor(ExecutorsFactory.executor()).build()

    val expiredDelivery : Counter = Counter.builder("expired_delivery_order")
        .tag("serviceName","p04")
        .description("Amount of orders failed to deliver because of failure of delivery service")
        .register(meterRegistry)

    val wrongTime : Counter = Counter.builder("refuned_due_to_wrong_time_prediction_order")
        .tag("serviceName","p04")
        .description("Amount of orders failed to deliver because of the wrong selected time")
        .register(meterRegistry)

    val timeslot_set_request_count: Counter = Counter.builder("timeslot_set_request_count")
    .tag("serviceName", "p04")
    .description("Set timeslots")
    .register(meterRegistry)

    val shipping_orders_total: Counter = Counter.builder("shipping_orders_total")
        .tag("serviceName", "p04")
        .description(" Total shipping orders")
        .register(meterRegistry)

    val current_shipping_orders: Counter = Counter.builder("current_shipping_orders")
        .tag("serviceName", "p04")
        .description("Current shipping orders")
        .register(meterRegistry)

    @Scheduled(fixedRate = 60000)
    override fun checkForRefund() {
        var time = (System.currentTimeMillis() / 1000).toInt()
        val orders = orderRepository.findInWindow((time - 61).toInt(), time.toInt())
        for (order in orders){
            if (order.status.equals(OrderStatus.SHIPPING)){
                meterRegistry.counter("order_status_changed","serviceName","p04",
                    "fromState",order.status.toString(),
                    "toState",OrderStatus.REFUND.toString()).increment()
                order.status = OrderStatus.REFUND
                expiredDelivery.increment()
                orderRepository.save(order)
                shipping_orders_total.increment()
            }
        }
    }

    override fun getDelivery(deliveryId: UUID): DeliveryModel {
        return deliveryRepository.findByIdOrNull(deliveryId)?.toModel() ?: throw NotFoundException("Order $deliveryId not found")
    }

    override fun getDeliveryByOrder(orderId: UUID): List<DeliveryModel> {
        return deliveryRepository.findByOrderId(orderId).map { it.toModel() }
    }


    override fun addDelivery(delivery: DeliveryModel) {
        timeslot_set_request_count.increment()
        current_shipping_orders.increment()
        deliveryRepository.save(delivery.toEntity())
        eventBus.post(DeliveryCreatedEvent(delivery))
        eventLogger.info(DeliveryServiceNotableEvents.I_DELIVERY_CREATED, delivery.toEntity())
    }

    override fun deleteDelivery(deliveryId: UUID) {
        val delivery = deliveryRepository.findByIdOrNull(deliveryId) ?: throw NotFoundException("Order $deliveryId not found")
        eventBus.post(DeliveryDeletedEvent(delivery.toModel()))
        eventLogger.info(DeliveryServiceNotableEvents.I_DELIVERY_DELETED, delivery)
        deliveryRepository.deleteById(deliveryId)
    }

    override fun finalizeDelivery(deliveryId: UUID) {
        val delivery = deliveryRepository.findByIdOrNull(deliveryId) ?: throw NotFoundException("Order $deliveryId not found")
        //there is nothing, yet
        eventBus.post(DeliveryDeletedEvent(delivery.toModel()))
        eventLogger.info(DeliveryServiceNotableEvents.I_DELIVERY_DELIVERED, delivery)
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "<= 0 number of slots")
    class InvalidNumberOfSlotsException : IllegalArgumentException()

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "unable to reach external service")
    class UnableToReachExternalServiceException : RuntimeException()


    override fun getDeliverySlots(number: Int): List<Int> {
        eventLogger.info(DeliveryServiceNotableEvents.I_DELIVERY_CHECK,number)
        if (number <= 0) {
            throw InvalidNumberOfSlotsException()
        }
        //access API, this transaction imitates receiving information about available slots
        val json = transaction()
        //calculate all available slots, choose number of first
        val temp : List<Int> = listOf(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30).minus(deliveryRepository.getAllSlots()
            .toSet())// some magic
        return temp.take(number)
    }

    override fun getDeliveryBySlot(slotInSec: Int) : DeliveryModel{
        val delivery = deliveryRepository.findBySlot(slotInSec)?.toModel() ?: throw NotFoundException("Slot $slotInSec not found")
        return delivery
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "cannot have a negative slot")
    class InvalidSlotException : IllegalArgumentException()

    override fun reserveDeliverySlots(orderId: UUID, slotInSec: Int) : BookingDto {
        //access API, this transaction imitates sending reservation
        if (slotInSec <= 0){
            throw InvalidSlotException()
        }
        val json = transaction()
        var order = orderRepository.findByIdOrNull(orderId)
//        if (order == null || !(order.status.equals(OrderStatus.PAID) || order.status.equals((OrderStatus.REFUND)))){
//            eventBus.post(SlotReserveReponseEvent(orderId,slotInSec,Status.FAILURE))
//            return Order().toBookingDto(setOf())
//        }
        //deliveryRepository.save(Delivery(orderId,null,slotInSec))
        if ((System.currentTimeMillis() / 1000).toInt() > order!!.deliveryDuration!!){
//            meterRegistry.counter("order_status_changed","serviceName","p04",
//                "fromState",order.status.toString(),
//            "toState",OrderStatus.REFUND.toString()).increment()
//            order.status = OrderStatus.REFUND
//            shipping_orders_total.increment()
//            orderRepository.save(order)
            meterRegistry.counter("refuned_due_to_wrong_time_prediction_order","serviceName","p04","accountId",order.userId.toString())
                .increment()
//            wrongTime.increment()
//            eventBus.post(SlotReserveReponseEvent(orderId,slotInSec,Status.FAILURE))
//            return Order().toBookingDto(setOf())
        }
        meterRegistry.counter("order_status_changed","serviceName","p04",
            "fromState",order!!.status.toString(),
            "toState",OrderStatus.SHIPPING.toString()).increment()
        order.status = OrderStatus.SHIPPING
        orderRepository.updateDeliveryDuration(orderId,slotInSec)
        shipping_orders_total.increment()
        eventBus.post(SlotReserveReponseEvent(orderId,slotInSec,Status.SUCCESS))
        return order.toBookingDto(setOf())
        //check if available and reserve
//        if (deliveryRepository.findBySlot(slotInSec) == null){
//            deliveryRepository.save(Delivery(deliveryId,null,slotInSec))
//            return json
//        } else {
//            throw IllegalArgumentException("Didn't found by Id or Slot already taken")
//        }
    }
    fun transaction() : JSONObject{
        var tries = 0
        val values = mapOf("clientSecret" to "832bce51-8cd5-4fda-8cb2-dd605c48069e")
        val objectMapper = ObjectMapper()
        val requestBody = objectMapper.writeValueAsString(values)
        val request = HttpRequest.newBuilder()
            .header("Content-Type", "application/json;IEEE754Compatible=true")
            .uri(URI.create("http://tps:8080/transactions/")) // replace with environment
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(10))
            .build()


        while(true){
            val requestTimer = System.nanoTime()
            tries++
            if (tries == 6) throw UnableToReachExternalServiceException()
            var future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//            var response = Unirest.post("http://77.234.215.138:30027/transactions/")
//                .header("Content-Type", "application/json;IEEE754Compatible=true")
//                .body("{\"clientSecret\": \"7d65037f-e9af-433e-8e3f-a3da77e019b1\"}")
//                .asJson()
            var response : HttpResponse<String>
            try{
                response = future.get()
            }
            catch (ex: ExecutionException)
            {
                DistributionSummary.builder("external_request_delivery")
                    .tags("serviceName","p04",
                        "httpCode","408",
                        "isTimeout",true.toString(),
                        "accountType","Transaction")
                    .publishPercentiles(0.75,0.9).register(meterRegistry)
                    .record((System.nanoTime() - requestTimer).toDouble())
                Thread.sleep(Math.pow(2.0,tries.toDouble()).toLong() * 500)
                continue
            }
            DistributionSummary.builder("external_request_delivery")
                .tags("serviceName","p04",
                    "httpCode",response.statusCode().toString(),
                    "isTimeout",false.toString(),
                    "accountType","Transaction")
                .publishPercentiles(0.75,0.9).register(meterRegistry)
                .record((System.nanoTime() - requestTimer).toDouble())
            if (response.statusCode() != 200){
                //wait
                Thread.sleep(Math.pow(2.0,tries.toDouble()).toLong() * 500)
                continue
            }
            var json = JSONObject(response.body())
            if (json.get("status").equals("SUCCESS")){
                return json
            }
            Thread.sleep(Math.pow(2.0,tries.toDouble()).toLong() * 500)
        }
    }
}