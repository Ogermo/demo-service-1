package com.itmo.microservices.demo.orders.impl.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.itmo.microservices.demo.orders.api.model.OrderStatus
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.web.bind.annotation.Mapping
import java.util.*
import javax.persistence.*
import kotlin.collections.HashMap

@Entity
@Table(name = "orders") // not order! will be problems
@JsonInclude(JsonInclude.Include.NON_NULL)
class Order {
    @Id
//    @Type(type = "uuid-char")
//    @GenericGenerator(
//        name = "UUID",
//        strategy = "org.hibernate.id.UUIDGenerator"
//    )
    var id : UUID? = null
    var timeCreated : Long = System.currentTimeMillis()
    var status : OrderStatus = OrderStatus.COLLECTING
    //var basketId : UUID? = null
    var userId : UUID = UUID.randomUUID()
    var deliveryDuration : Int? = null


    constructor()

    constructor(id : UUID?, timeCreated : Long, status : OrderStatus, userId : UUID, deliveryDuration : Int? = null) {

        if (id == null){
            this.id = UUID.randomUUID()
        }
        else
        {
            this.id = id
        }

        this.status = status
        this.userId = userId
        this.timeCreated = timeCreated
        this.deliveryDuration = deliveryDuration
    }

}