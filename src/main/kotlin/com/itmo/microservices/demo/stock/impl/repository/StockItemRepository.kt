package com.itmo.microservices.demo.stock.impl.repository

import com.itmo.microservices.demo.stock.impl.entity.StockItem
import com.itmo.microservices.demo.users.impl.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StockItemRepository : JpaRepository<StockItem, UUID> {
    @Query("From StockItem WHERE title = ?1")
    fun findByTitle(title: String) : StockItem?
}