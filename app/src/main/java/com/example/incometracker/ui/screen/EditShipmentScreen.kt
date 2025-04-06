package com.example.incometracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.model.Product
import com.example.incometracker.data.model.Shipment
import com.example.incometracker.data.model.ShipmentStatus
import com.example.incometracker.ui.viewmodel.ShipmentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShipmentScreen(
    shipmentId: Int,
    viewModel: ShipmentViewModel,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // State variables for form fields
    var selectedProduct by rememberSaveable { mutableStateOf<Product?>(null) }
    var quantity by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var selectedStatus by rememberSaveable { mutableStateOf<ShipmentStatus?>(null) }
    var returnedQuantity by rememberSaveable { mutableStateOf("0") }
    
    // State for dropdown expanded status
    var productExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    
    // State for validation errors
    var hasErrors by remember { mutableStateOf(false) }
    
    // Fetch the existing shipment data
    LaunchedEffect(shipmentId) {
        val shipment = viewModel.getShipmentById(shipmentId)
        shipment?.let {
            selectedProduct = it.product
            quantity = it.quantity.toString()
            destination = it.destination
            selectedStatus = it.status
            returnedQuantity = it.returnedQuantity.toString()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Shipment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Validate inputs
                    hasErrors = selectedProduct == null || 
                               quantity.isBlank() || 
                               quantity.toIntOrNull() == null || 
                               quantity.toIntOrNull()!! <= 0 ||
                               destination.isBlank() ||
                               selectedStatus == null ||
                               returnedQuantity.toIntOrNull() == null ||
                               returnedQuantity.toIntOrNull()!! < 0 ||
                               (returnedQuantity.toIntOrNull() ?: 0) > (quantity.toIntOrNull() ?: 0)
                               
                    if (!hasErrors) {
                        // Submit update
                        coroutineScope.launch {
                            viewModel.updateShipment(
                                shipmentId = shipmentId,
                                product = selectedProduct!!,
                                quantity = quantity.toInt(),
                                destination = destination,
                                status = selectedStatus!!,
                                returnedQuantity = returnedQuantity.toInt()
                            )
                            onNavigateBack()
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = productExpanded,
                onExpandedChange = { productExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Product") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = productExpanded,
                    onDismissRequest = { productExpanded = false }
                ) {
                    // List all available products
                    Product.values().forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.displayName) },
                            onClick = {
                                selectedProduct = product
                                productExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Quantity input
            OutlinedTextField(
                value = quantity,
                onValueChange = { 
                    // Only allow digits
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        quantity = it
                    }
                },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = hasErrors && (quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toIntOrNull()!! <= 0)
            )
            
            // Destination input
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text("Destination") },
                modifier = Modifier.fillMaxWidth(),
                isError = hasErrors && destination.isBlank()
            )
            
            // Status Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus?.displayName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = hasErrors && selectedStatus == null
                )
                
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    // List all available statuses
                    ShipmentStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayName) },
                            onClick = {
                                selectedStatus = status
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Only show returned quantity for COMPLETE status
            if (selectedStatus == ShipmentStatus.COMPLETE) {
                // Returned Quantity input
                OutlinedTextField(
                    value = returnedQuantity,
                    onValueChange = { 
                        // Only allow digits
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            returnedQuantity = it
                        }
                    },
                    label = { Text("Returned Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = hasErrors && (
                        returnedQuantity.toIntOrNull() == null || 
                        returnedQuantity.toIntOrNull()!! < 0 ||
                        (returnedQuantity.toIntOrNull() ?: 0) > (quantity.toIntOrNull() ?: 0)
                    )
                )
                
                // Help text for returned quantity
                Text(
                    text = "Enter the number of items returned by the store. Must be less than or equal to the total quantity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }
            
            if (hasErrors) {
                Text(
                    text = "Please fill all fields correctly. Returned quantity must be less than or equal to total quantity.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 