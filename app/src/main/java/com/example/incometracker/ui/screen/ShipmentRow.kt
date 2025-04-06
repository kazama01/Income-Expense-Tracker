package com.example.incometracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.model.Shipment
import com.example.incometracker.data.model.ShipmentStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Simple date formatter
private val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

// Currency formatter for Indonesian Rupiah
private val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
    currency = Currency.getInstance("IDR")
}

@Composable
fun ShipmentRow(
    shipment: Shipment,
    onEditClick: (Int) -> Unit = {} // Default no-op handler
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Add space between text lines
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Product: ${shipment.product.displayName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    // Status badge
                    val statusColor = when(shipment.status) {
                        ShipmentStatus.IN_PROGRESS -> Color(0xFFFFA000) // Amber
                        ShipmentStatus.COMPLETE -> Color(0xFF4CAF50)    // Green
                    }
                    
                    Surface(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = shipment.status.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                // Edit button
                IconButton(
                    onClick = { onEditClick(shipment.id) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Shipment",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Quantity information with returns if applicable
            if (shipment.returnedQuantity > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quantity: ${shipment.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Returned: ${shipment.returnedQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE53935) // Red for returned
                    )
                }
                
                Text(
                    text = "Final Quantity: ${shipment.effectiveQuantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Quantity: ${shipment.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price: ${currencyFormatter.format(shipment.priceAtTime)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Total: ${currencyFormatter.format(shipment.totalValue)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Destination: ${shipment.destination}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Show dates
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Created: ${dateFormatter.format(shipment.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show completion date if completed
                if (shipment.status == ShipmentStatus.COMPLETE && shipment.completionDate != null) {
                    Text(
                        text = "Completed: ${dateFormatter.format(shipment.completionDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50), // Green for completion
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 