package com.itmo.microservices.demo.payment.impl.util

import com.itmo.microservices.demo.orders.api.model.PaymentLogRecordDto
import com.itmo.microservices.demo.orders.impl.entity.PaymentStatus
import com.itmo.microservices.demo.payment.impl.entity.Payment


fun List<Payment>.toDto() : List<PaymentLogRecordDto> {
    val paymentList = mutableListOf<PaymentLogRecordDto>()
    for (value in this){
        paymentList.add(
            PaymentLogRecordDto(value.openTime!!,
            PaymentStatus.SUCCESS,
                value.amount!!,
                value.transactionId!!))
    }
    return paymentList
}