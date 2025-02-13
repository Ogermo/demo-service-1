package com.itmo.microservices.demo.orders.impl.entity

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "order_items")
class OrderItems {
    @Id
    @Type(type = "uuid-char")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    var id : UUID = UUID.randomUUID()
    var orderId : UUID? = null
    var itemId : UUID? = null
    var amount : Int? = null

    constructor()

    constructor(id : UUID?,orderId : UUID, itemId : UUID, amount : Int){
        if (id != null){
            this.id = id
        } else {
            this.id = UUID.randomUUID()
        }
        this.orderId = orderId
        this.itemId = itemId
        this.amount = amount
    }
}