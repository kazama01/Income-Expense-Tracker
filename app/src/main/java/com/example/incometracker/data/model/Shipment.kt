package com.example.incometracker.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity class that represents a shipment in the database
 */
@Entity(tableName = "shipments")
data class Shipment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val product: Product,
    val quantity: Int,
    val destination: String,
    val timestamp: Date,
    val priceAtTime: Double,
    val status: ShipmentStatus = ShipmentStatus.IN_PROGRESS, // Default to IN_PROGRESS
    val returnedQuantity: Int = 0, // Default to 0 returned products
    val completionDate: Date? = null // Date when the shipment was marked as complete
) {
    // Calculate the total value based on quantity, price, and returns
    @Ignore
    val totalValue: Double = (quantity - returnedQuantity) * priceAtTime
    
    // Calculate the effective quantity (after returns)
    @Ignore
    val effectiveQuantity: Int = quantity - returnedQuantity
    
    // Secondary constructor to make initialization easier
    constructor(
        product: Product,
        quantity: Int,
        destination: String,
        timestamp: Date,
        priceAtTime: Double = product.price // Default to current price
    ) : this(
        id = 0,
        product = product,
        quantity = quantity,
        destination = destination,
        timestamp = timestamp,
        priceAtTime = priceAtTime,
        status = ShipmentStatus.IN_PROGRESS,
        returnedQuantity = 0,
        completionDate = null
    )
} 