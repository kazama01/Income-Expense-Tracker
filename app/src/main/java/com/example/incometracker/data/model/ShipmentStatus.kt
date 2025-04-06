package com.example.incometracker.data.model

/**
 * Represents the status of a shipment
 */
enum class ShipmentStatus(val displayName: String) {
    IN_PROGRESS("In Progress"),   // Store hasn't paid yet (payment upon sale)
    COMPLETE("Complete")          // Shipment is complete with payment received
} 