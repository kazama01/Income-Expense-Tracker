package com.example.incometracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.model.Product
import com.example.incometracker.ui.viewmodel.ShipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShipmentScreen(
    viewModel: ShipmentViewModel,
    onNavigateBack: () -> Unit // Callback to navigate back
) {
    var selectedProduct by remember { mutableStateOf(Product.values()[0]) } // Default to first product
    var quantityString by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // State for Dropdown menu
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Shipment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Product Selector (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedProduct.displayName,
                    onValueChange = { }, // Display only
                    label = { Text("Product") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(), // Use default colors
                    modifier = Modifier
                        .menuAnchor() // Required for ExposedDropdownMenuBox
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    Product.values().forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.displayName) },
                            onClick = {
                                selectedProduct = product
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding, // Recommended padding
                        )
                    }
                }
            }

            // Quantity Input
            OutlinedTextField(
                value = quantityString,
                onValueChange = { quantityString = it.filter { char -> char.isDigit() } }, // Allow only digits
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && quantityString.toIntOrNull() == null
            )

            // Destination Input
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && destination.isBlank()
            )

            if (showError) {
                Text(
                    text = "Please fill in all fields correctly.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val quantity = quantityString.toIntOrNull()
                    if (quantity != null && quantity > 0 && destination.isNotBlank()) {
                        showError = false
                        isSaving = true
                        viewModel.addShipment(selectedProduct, quantity, destination)
                        // Simulate save delay and navigate back
                        // In a real app, you might wait for DB confirmation
                        onNavigateBack() // Navigate back immediately
                    } else {
                        showError = true
                    }
                },
                enabled = !isSaving, // Disable button while saving
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save Shipment")
            }
        }
    }
} 