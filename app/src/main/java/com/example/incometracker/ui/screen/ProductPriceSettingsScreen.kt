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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.model.Product
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPriceSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Store price input values for each product
    val priceInputValues = remember {
        Product.values().associate { product ->
            product to mutableStateOf(product.price.toString())
        }
    }

    // Store error states for each product
    val priceErrors = remember {
        Product.values().associate { product ->
            product to mutableStateOf(false)
        }
    }

    // Format for displaying prices
    val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("IDR")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Price Settings") },
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
                    // Validate and save all prices
                    var hasError = false
                    
                    // Update prices for each product if valid
                    Product.values().forEach { product ->
                        val priceString = priceInputValues[product]?.value ?: ""
                        val priceValue = priceString.toDoubleOrNull()
                        
                        if (priceValue != null && priceValue > 0) {
                            product.updatePrice(priceValue)
                            priceErrors[product]?.value = false
                        } else {
                            priceErrors[product]?.value = true
                            hasError = true
                        }
                    }
                    
                    if (!hasError) {
                        // Save to preferences
                        Product.savePricesToPreferences(context)
                        // Navigate back
                        onNavigateBack()
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
            Text(
                text = "Set Global Product Prices",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = "These prices will be used for all future shipment calculations",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price input fields for each product
            Product.values().forEach { product ->
                PriceInputField(
                    product = product,
                    priceValue = priceInputValues[product]?.value ?: "",
                    onPriceChanged = { newValue ->
                        priceInputValues[product]?.value = newValue
                    },
                    isError = priceErrors[product]?.value ?: false,
                    currencyFormatter = currencyFormatter
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceInputField(
    product: Product,
    priceValue: String,
    onPriceChanged: (String) -> Unit,
    isError: Boolean,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = product.displayName,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = priceValue,
                onValueChange = { newValue ->
                    // Filter to only allow digits and decimals
                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                    onPriceChanged(filtered)
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = isError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = {
                    if (isError) {
                        Text("Please enter a valid price")
                    } else {
                        val currentValue = priceValue.toDoubleOrNull() ?: 0.0
                        Text("Current value: ${currencyFormatter.format(currentValue)}")
                    }
                }
            )
        }
    }
} 