package com.itmo.microservices.demo.shoppingCartService.impl.entity


import java.util.*
import javax.persistence.*

@Entity
@Table(name="cart")
class ShoppingCart {
    @Id
    var id : UUID = UUID.randomUUID()
    var status: String = "ACTIVE"


    constructor()

    constructor(id: UUID, status: String) {
        this.id = id
        this.status = status
    }
}