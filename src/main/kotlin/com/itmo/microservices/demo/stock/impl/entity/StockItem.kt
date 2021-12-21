package com.itmo.microservices.demo.stock.impl.entity

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "stock")
class StockItem {

    @Id
    var id: UUID? = null
    var title: String? = ""
    var description: String? = ""
    var price: Int? = 100
    var amount: Int? = 0

    constructor()

    constructor(id: UUID? = null, title: String? = "", description: String? = "", price: Int? = 100,
                amount: Int? = 0) {
        if (id == null){
            this.id = UUID.randomUUID()
        }
        else
        {
            this.id = id
        }
        this.title = title
        this.description = description
        this.price = price
        this.amount = amount
    }

    override fun toString(): String =
        "StockItem(id=$id, title=$title, description=$description, price=$price, amount=$amount)"

    fun addAmount(number: Int) {
        this.amount = amount?.plus(number)
    }
    
    fun removeAmount(number: Int) {
        this.amount = amount?.minus(number)
    }

}
