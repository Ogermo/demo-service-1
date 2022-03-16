package com.itmo.microservices.demo.payment.impl.service

import com.google.common.eventbus.EventBus
import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
import com.itmo.microservices.demo.delivery.api.model.Status
import com.itmo.microservices.demo.delivery.impl.service.DefaultDeliveryService
import com.itmo.microservices.demo.orders.api.event.PaymentCreatedEvent
import com.itmo.microservices.demo.orders.api.event.SlotReserveReponseEvent
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import com.itmo.microservices.demo.orders.impl.repository.OrderRepository
import com.itmo.microservices.demo.payment.api.model.PaymentSubmissionDto
import com.itmo.microservices.demo.payment.api.model.UserAccountFinancialLogRecordDto
import com.itmo.microservices.demo.payment.api.service.PaymentService
import com.itmo.microservices.demo.payment.api.util.FinancialOperationType
import com.itmo.microservices.demo.payment.impl.entity.Payment
import com.itmo.microservices.demo.payment.impl.logging.PaymentServiceNotableEvents
import com.itmo.microservices.demo.payment.impl.repository.PaymentRepository
import org.springframework.stereotype.Service
import java.sql.Time
import java.util.*
import kotlin.random.Random
import kong.unirest.Unirest
import kong.unirest.json.JSONObject
import com.itmo.microservices.demo.payment.impl.util.PaymentServiceMeta
import com.itmo.microservices.demo.stock.api.event.DeductItemEvent
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException
import java.lang.StringBuilder

@Service
class DefaultPaymentService(private val paymentRepository: PaymentRepository,
                            private val orderRepository: OrderRepository,
                            private val eventBus: EventBus) : PaymentService {

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "unable to reach external service")
    class UnableToReachExternalServiceException : RuntimeException()

    fun makeTransaction() : JSONObject {
        val url = PaymentServiceMeta.makeTransactionUri()

        var response = Unirest.post(url)
                .header("Content-Type", "application/json;IEEE754Compatible=true")
                .body("{\"clientSecret\": \"7d65037f-e9af-433e-8e3f-a3da77e019b1\"}")
                .asJson()

        var tries = 3

        while(tries > 0) {
            response = Unirest.post(url)
                .header("Content-Type", "application/json;IEEE754Compatible=true")
                .body("{\"clientSecret\": \"7d65037f-e9af-433e-8e3f-a3da77e019b1\"}")
                .asJson()

            if (response.status == 200)
                break

            tries--
        }

        val json = response.body.`object`

        if (response.status == 200) {
            return json
        }

        val sb = StringBuilder()

        sb.append(response.status.toString())
                .append(" ")
                .append(json.get("message").toString())
                .append(" at timestamp ")
                .append(json.get("timestamp").toString())

        eventLogger.error(PaymentServiceNotableEvents.I_MAKE_TRANSACTION_FAILURE, sb.toString())

        throw UnableToReachExternalServiceException()
    }

    override fun getFinLog(orderId: UUID?): List<UserAccountFinancialLogRecordDto> {
        val logs = mutableListOf<UserAccountFinancialLogRecordDto>()
        var orderPayments : List<Payment>
        if (orderId != null){
            orderPayments = paymentRepository.findByOrderId(orderId)
            eventLogger.info(PaymentServiceNotableEvents.I_FINANCIAL_LOGS_GIVEN, orderId)
        }
        else {
            orderPayments = paymentRepository.findAll()
        }
        orderPayments.forEach {
            val type: FinancialOperationType = if (it.type == 0) FinancialOperationType.WITHDRAW
            else FinancialOperationType.REFUND
            logs.add(UserAccountFinancialLogRecordDto(type, it.amount!!, it.orderId!!, it.transactionId!!, it.closeTime!!))
        }

        return logs
    }

    override fun makePayment(orderId: UUID): PaymentSubmissionDto {

        //order need to be booked
        var order = orderRepository.findByIdOrNull(orderId) ?: throw IllegalArgumentException()
        if (!order.status.equals(OrderStatus.BOOKED)){
            eventBus.post(PaymentCreatedEvent(orderId,null, Status.FAILURE))
            return PaymentSubmissionDto(0, UUID.fromString("0-0-0-0-0"))
        }


        val transaction = makeTransaction()

        if (transaction.isEmpty) {
            eventBus.post(PaymentCreatedEvent(orderId,null, Status.FAILURE))
            return PaymentSubmissionDto(0, UUID.fromString("0-0-0-0-0"))
        }

        val id = UUID.fromString(transaction.get("id").toString())
        val status = transaction.get("status").toString()

        if (status == "FAILURE") {
            eventBus.post(PaymentCreatedEvent(orderId,null, Status.FAILURE))
            return PaymentSubmissionDto(0, UUID.fromString("0-0-0-0-0"))
        }

        val cost = transaction.get("cost").toString().toInt()
        val submitTime = transaction.get("submitTime").toString().toLong()
        val completedTime = transaction.get("completedTime").toString().toLong()

        val submissionDto = PaymentSubmissionDto(completedTime, id)

        val payment = Payment()

        payment.Id = UUID.randomUUID()
        payment.orderId = orderId
        payment.transactionId = id
        payment.openTime = submitTime
        payment.closeTime = completedTime
        payment.type = 0
        payment.amount = cost

        paymentRepository.save(payment)

        eventLogger.info(PaymentServiceNotableEvents.I_PAYMENT_CREATED, submissionDto)
        eventBus.post(PaymentCreatedEvent(orderId,payment.transactionId, Status.SUCCESS))

        return submissionDto
    }

    override fun refundPayment(orderId: UUID, cost: Double): PaymentSubmissionDto {
        return PaymentSubmissionDto(1234, orderId)
    }

}