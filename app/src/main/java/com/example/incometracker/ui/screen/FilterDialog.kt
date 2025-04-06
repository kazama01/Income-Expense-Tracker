package com.example.incometracker.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.incometracker.data.model.Product
import com.example.incometracker.data.model.ShipmentFilter
import com.example.incometracker.data.model.ShipmentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: ShipmentFilter,
    onDismiss: () -> Unit,
    onApplyFilter: (ShipmentFilter) -> Unit,
    onClearFilter: () -> Unit
) {
    // Current date for date picker
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    // State for the filter options
    var selectedProduct by rememberSaveable { mutableStateOf(currentFilter.product) }
    var selectedStatus by rememberSaveable { mutableStateOf(currentFilter.status) }
    
    // State for date range
    var startDate by rememberSaveable { mutableStateOf(currentFilter.startDate) }
    var endDate by rememberSaveable { mutableStateOf(currentFilter.endDate) }
    
    // State for year and month selection
    var selectedYear by rememberSaveable { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by rememberSaveable { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    
    // State for filter type (specific month or date range)
    var dateFilterType by rememberSaveable { mutableStateOf(
        if (currentFilter.monthYear != null) "month" else if (currentFilter.startDate != null) "range" else "none"
    ) }
    
    // State for dropdown expanded status
    var productExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    
    // Date picker state
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate?.time ?: System.currentTimeMillis()
    )
    var showStartDatePicker by remember { mutableStateOf(false) }
    
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = endDate?.time ?: System.currentTimeMillis()
    )
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Show date picker dialogs if needed
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    startDatePickerState.selectedDateMillis?.let {
                        startDate = Date(it)
                    }
                    showStartDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    endDatePickerState.selectedDateMillis?.let {
                        endDate = Date(it)
                    }
                    showEndDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
    
    // Main filter dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Shipments",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Product Filter
                Text(
                    text = "Filter by Product",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedProduct?.displayName ?: "All Products",
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
                        // All products option
                        DropdownMenuItem(
                            text = { Text("All Products") },
                            onClick = {
                                selectedProduct = null
                                productExpanded = false
                            }
                        )
                        
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
                
                // Status Filter
                Text(
                    text = "Filter by Status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedStatus?.displayName ?: "All Statuses",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        // All statuses option
                        DropdownMenuItem(
                            text = { Text("All Statuses") },
                            onClick = {
                                selectedStatus = null
                                statusExpanded = false
                            }
                        )
                        
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
                
                // Date Filter
                Text(
                    text = "Filter by Date",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                
                // Date filter type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = dateFilterType == "none",
                        onClick = { dateFilterType = "none" },
                        label = { Text("No Date Filter") }
                    )
                    
                    FilterChip(
                        selected = dateFilterType == "month",
                        onClick = { dateFilterType = "month" },
                        label = { Text("By Month") }
                    )
                    
                    FilterChip(
                        selected = dateFilterType == "range",
                        onClick = { dateFilterType = "range" },
                        label = { Text("Date Range") }
                    )
                }
                
                // Show appropriate date filter based on selection
                when (dateFilterType) {
                    "month" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Month dropdown
                            ExposedDropdownMenuBox(
                                expanded = monthExpanded,
                                onExpandedChange = { monthExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = when (selectedMonth) {
                                        1 -> "January"
                                        2 -> "February"
                                        3 -> "March"
                                        4 -> "April"
                                        5 -> "May"
                                        6 -> "June"
                                        7 -> "July"
                                        8 -> "August"
                                        9 -> "September"
                                        10 -> "October"
                                        11 -> "November"
                                        12 -> "December"
                                        else -> "January"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Month") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = monthExpanded,
                                    onDismissRequest = { monthExpanded = false }
                                ) {
                                    listOf(
                                        1 to "January",
                                        2 to "February",
                                        3 to "March",
                                        4 to "April",
                                        5 to "May",
                                        6 to "June",
                                        7 to "July",
                                        8 to "August",
                                        9 to "September",
                                        10 to "October",
                                        11 to "November",
                                        12 to "December"
                                    ).forEach { (month, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedMonth = month
                                                monthExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Year dropdown
                            ExposedDropdownMenuBox(
                                expanded = yearExpanded,
                                onExpandedChange = { yearExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = selectedYear.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Year") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                    modifier = Modifier.menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = yearExpanded,
                                    onDismissRequest = { yearExpanded = false }
                                ) {
                                    // List last 5 years
                                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                                    (currentYear downTo currentYear - 4).forEach { year ->
                                        DropdownMenuItem(
                                            text = { Text(year.toString()) },
                                            onClick = {
                                                selectedYear = year
                                                yearExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "range" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start Date
                            OutlinedTextField(
                                value = startDate?.let { dateFormatter.format(it) } ?: "Select Start Date",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Start Date") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showStartDatePicker = true }
                            )
                            
                            // End Date
                            OutlinedTextField(
                                value = endDate?.let { dateFormatter.format(it) } ?: "Select End Date",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("End Date") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEndDatePicker = true }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearFilter,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Filters")
                    }
                    
                    Button(
                        onClick = {
                            val newFilter = when(dateFilterType) {
                                "month" -> ShipmentFilter.forMonthYear(selectedYear, selectedMonth)
                                    .copy(product = selectedProduct, status = selectedStatus)
                                "range" -> ShipmentFilter(
                                    product = selectedProduct,
                                    status = selectedStatus,
                                    startDate = startDate,
                                    endDate = endDate
                                )
                                else -> ShipmentFilter(
                                    product = selectedProduct,
                                    status = selectedStatus
                                )
                            }
                            onApplyFilter(newFilter)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
} 