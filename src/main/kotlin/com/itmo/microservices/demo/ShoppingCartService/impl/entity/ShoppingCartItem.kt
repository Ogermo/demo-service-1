package com.itmo.microservices.demo.ShoppingCartService.impl.entity

import java.util.UUID
import javax.persistence.*

@Entity
class ShoppingCartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    lateinit var id: UUID

    var shoppingCartID: UUID = UUID.randomUUID()
    var catalogItemID: UUID = UUID.randomUUID()


    constructor()

    constructor(shoppingCartID: UUID, catalogItemID: UUID) {
        this.shoppingCartID = shoppingCartID
        this.catalogItemID = catalogItemID
    }
}