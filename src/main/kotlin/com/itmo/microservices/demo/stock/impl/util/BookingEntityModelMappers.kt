package com.itmo.microservices.demo.stock.impl.util

import com.itmo.microservices.demo.stock.api.model.BookingLogRecordModel
import com.itmo.microservices.demo.stock.api.model.CatalogItemDto
import com.itmo.microservices.demo.stock.api.model.StockItemModel
import com.itmo.microservices.demo.stock.impl.entity.BookingLogRecord
import com.itmo.microservices.demo.stock.impl.entity.StockItem

fun BookingLogRecordModel.toEntity(): BookingLogRecord = BookingLogRecord(
    id = this.id,
    bookingId = this.bookingId,
    itemId = this.itemId,
    status = this.status,
    amount = this.amount,
    timestamp = this.timestamp
)

fun BookingLogRecord.toModel(): BookingLogRecordModel = BookingLogRecordModel(
    id = this.id,
    bookingId = this.bookingId,
    itemId = this.itemId,
    status = this.status,
    amount = this.amount,
    timestamp = this.timestamp
)
