package com.itmo.microservices.demo.payment.impl.repository

import com.itmo.microservices.demo.payment.impl.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface PaymentRepository : JpaRepository<Payment, UUID> {

    @Query("FROM Payment WHERE orderId = ?1")
    fun findByOrderId(orderId: UUID): List<Payment>
}