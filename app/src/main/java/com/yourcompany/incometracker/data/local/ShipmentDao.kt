package com.yourcompany.incometracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.yourcompany.incometracker.data.model.Shipment
import kotlinx.coroutines.flow.Flow // Import Flow

@Dao
interface ShipmentDao {

    // Insert a single shipment
    @Insert
    suspend fun insertShipment(shipment: Shipment)

    // Get all shipments, ordered by date descending, as a Flow
    @Query("SELECT * FROM shipments ORDER BY date DESC")
    fun getAllShipments(): Flow<List<Shipment>> // Return Flow<List<Shipment>>

    // Get a single shipment by its ID (useful for potential future editing/details view)
    @Query("SELECT * FROM shipments WHERE id = :shipmentId")
    suspend fun getShipmentById(shipmentId: Int): Shipment?

    // Delete a specific shipment
    @Delete
    suspend fun deleteShipment(shipment: Shipment)

    // Clear all shipments (useful for debugging or reset functionality)
    @Query("DELETE FROM shipments")
    suspend fun deleteAllShipments()
} 