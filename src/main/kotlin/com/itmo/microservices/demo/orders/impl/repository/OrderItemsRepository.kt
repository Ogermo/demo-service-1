package com.itmo.microservices.demo.orders.impl.repository

import com.itmo.microservices.demo.orders.impl.entity.OrderItems
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface OrderItemsRepository : JpaRepository<OrderItems, UUID>, JpaSpecificationExecutor<OrderItems> {

    @Query("FROM OrderItems WHERE orderId = ?1")
    fun findByOrderId(OrderId: UUID): List<OrderItems>

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM OrderItems WHERE orderId = ?1"
        ,nativeQuery = true)
    fun deleteByOrderId(OrderId: UUID)
}