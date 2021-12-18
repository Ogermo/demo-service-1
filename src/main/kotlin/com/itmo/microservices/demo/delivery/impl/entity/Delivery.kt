package com.itmo.microservices.demo.delivery.impl.entity

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "delivery")
class Delivery {
    @Id
    var orderId : UUID? = null
    var address : String? = null
    var slot : Int? = null

    constructor()

    constructor(orderId : UUID?, address: String?) {
        this.orderId = orderId
        this.address = address
    }

    constructor(orderId : UUID?, address: String?, slot: Int?) {
        this.orderId = orderId
        this.address = address
        this.slot = slot
    }
}