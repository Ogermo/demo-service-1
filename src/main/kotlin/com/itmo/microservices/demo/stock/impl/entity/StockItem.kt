package com.itmo.microservices.demo.stock.impl.entity

import com.itmo.microservices.demo.stock.api.model.Category
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
    var reservedCount: Int? = 0
    var category: Category = Category.COMMON

    constructor()

    constructor(id: UUID? = null, title: String? = "", description: String? = "", price: Int? = 100,
                amount: Int? = 0, reservedCount: Int? = 0, category: Category) {
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
        this.reservedCount = reservedCount
        this.category = category
    }

    override fun toString(): String =
        "StockItem(id=$id, title=$title, description=$description, price=$price, amount=$amount, " +
                "reservedCount=$reservedCount, category=$category)"

    fun setReservedCount(number: Int) {
        this.reservedCount = reservedCount?.plus(number)
    }

    fun setAmount(number: Int) {
        this.amount = amount?.plus(number)
    }

}
