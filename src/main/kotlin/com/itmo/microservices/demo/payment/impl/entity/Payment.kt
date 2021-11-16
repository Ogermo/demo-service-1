package com.itmo.microservices.demo.payment.impl.entity

import java.sql.Time
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "payment304")
data class Payment (
    @Id
    val orderId : UUID = UUID.randomUUID(),
    val type : Int = 0,
    val amount : Int = 0,
    val time : Time? = null
)