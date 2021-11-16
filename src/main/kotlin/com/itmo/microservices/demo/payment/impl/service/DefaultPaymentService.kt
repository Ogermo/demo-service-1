package com.itmo.microservices.demo.payment.impl.service

import com.itmo.microservices.commonlib.annotations.InjectEventLogger
import com.itmo.microservices.commonlib.logging.EventLogger
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

@Service
class DefaultPaymentService(private val paymentRepository: PaymentRepository) : PaymentService {

    @InjectEventLogger
    private lateinit var eventLogger: EventLogger

    override fun getFinLog(orderId: UUID): List<UserAccountFinancialLogRecordDto> {
        val logs = mutableListOf<UserAccountFinancialLogRecordDto>()
        val orderPayments = paymentRepository.findByOrderId(orderId)

        orderPayments.forEach {
            logs.add(UserAccountFinancialLogRecordDto(FinancialOperationType.WITHDRAW, it.amount, it.orderId, UUID.randomUUID(), Random(0).nextLong()))
        }

        eventLogger.info(PaymentServiceNotableEvents.I_FINANCIAL_LOGS_GIVEN, orderId)

        return logs
    }

    override fun makePayment(orderId: UUID): PaymentSubmissionDto {
        val payment = paymentRepository.save(Payment(orderId, 0, 1, Time.valueOf("11:11:11")))

        val localTime = payment.time?.toLocalTime()
        var timestamp : Long = 0
        if (localTime != null) {
            timestamp = localTime.second.toLong()
        }

        val submissionDto = PaymentSubmissionDto(timestamp, UUID.randomUUID())

        eventLogger.info(PaymentServiceNotableEvents.I_PAYMENT_CREATED, submissionDto)

        return submissionDto
    }

}