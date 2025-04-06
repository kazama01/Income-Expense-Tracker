package com.example.incometracker.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.incometracker.data.model.Shipment
import com.example.incometracker.data.model.ShipmentFilter
import com.example.incometracker.data.model.ShipmentStatus
import com.example.incometracker.ui.viewmodel.ShipmentViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipmentListScreen(
    viewModel: ShipmentViewModel,
    onAddShipmentClick: () -> Unit, // Callback for navigation to add shipment
    onSettingsClick: () -> Unit, // Callback for navigation to settings
    onEditShipmentClick: (Int) -> Unit, // Callback for navigation to edit shipment
    onMonthlyReportClick: () -> Unit // Callback for navigation to monthly report
) {
    // Observe the LiveData from the ViewModel
    val allShipmentsList = viewModel.allShipments.observeAsState(initial = emptyList()).value
    val inProgressShipmentsList = viewModel.inProgressShipments.observeAsState(initial = emptyList()).value
    val completedShipmentsList = viewModel.completedShipments.observeAsState(initial = emptyList()).value
    
    // Get the filtered shipments (if any)
    val filteredShipments = viewModel.filteredShipments.observeAsState(initial = emptyList()).value
    
    // Get the current filter state
    val currentFilter = viewModel.currentFilter.observeAsState(initial = ShipmentFilter()).value
    
    // Observe the various value totals
    val totalValue = viewModel.getTotalValue().observeAsState(initial = 0.0).value
    val inProgressValue = viewModel.getInProgressValue().observeAsState(initial = 0.0).value
    val completedValue = viewModel.getCompletedValue().observeAsState(initial = 0.0).value
    val filteredTotalValue = viewModel.getFilteredShipmentsTotalValue().observeAsState(initial = 0.0).value
    
    // Tab selection state for status tabs (used when no filters active)
    var selectedTabIndex by remember { mutableIntStateOf(1) }
    
    // Filter dialog state
    var showFilterDialog by remember { mutableStateOf(false) }
    
    // Format for displaying dates
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    
    // Currency formatter for Indonesian Rupiah
    val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("IDR")
    }
    
    // Show filter dialog if needed
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = currentFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filter ->
                viewModel.applyFilter(filter)
            },
            onClearFilter = {
                viewModel.clearFilter()
                showFilterDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipment History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    // Monthly Report button
                    IconButton(onClick = onMonthlyReportClick) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = "Monthly Reports"
                        )
                    }
                
                    // Filter button
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Filled.FilterAlt,
                            contentDescription = "Filter Shipments"
                        )
                    }
                    
                    // Settings button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Price Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddShipmentClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Shipment")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Show filter chips if filters are active
            if (currentFilter.hasFilters()) {
                // Filter indicator bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Active Filters:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Row(
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Product filter
                            if (currentFilter.product != null) {
                                FilterChip(
                                    selected = true,
                                    onClick = { /* do nothing */ },
                                    label = { Text("Product: ${currentFilter.product.displayName}") },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            
                            // Status filter
                            if (currentFilter.status != null) {
                                FilterChip(
                                    selected = true,
                                    onClick = { /* do nothing */ },
                                    label = { Text("Status: ${currentFilter.status.displayName}") },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                        
                        // Date filter
                        if (currentFilter.monthYear != null) {
                            val parts = currentFilter.monthYear.split("-")
                            if (parts.size == 2) {
                                val month = parts[0].toIntOrNull()
                                val year = parts[1].toIntOrNull()
                                if (month != null && year != null) {
                                    val monthName = when (month) {
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
                                        else -> "Unknown"
                                    }
                                    FilterChip(
                                        selected = true,
                                        onClick = { /* do nothing */ },
                                        label = { Text("Month: $monthName $year") }
                                    )
                                }
                            }
                        } else if (currentFilter.startDate != null && currentFilter.endDate != null) {
                            FilterChip(
                                selected = true,
                                onClick = { /* do nothing */ },
                                label = { 
                                    Text("Date: ${dateFormatter.format(currentFilter.startDate)} - ${dateFormatter.format(currentFilter.endDate)}") 
                                }
                            )
                        }
                        
                        TextButton(
                            onClick = { viewModel.clearFilter() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            } else {
                // If no filters, show standard tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("All") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("In Progress") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Complete") }
                    )
                }
            }
            
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (currentFilter.hasFilters()) {
                            "Filtered Shipments"
                        } else {
                            when (selectedTabIndex) {
                                0 -> "All Shipments"
                                1 -> "In Progress Shipments"
                                else -> "Completed Shipments"
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Shipment count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Shipments: ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (currentFilter.hasFilters()) {
                                "${filteredShipments.size}"
                            } else {
                                when (selectedTabIndex) {
                                    0 -> "${allShipmentsList.size}"
                                    1 -> "${inProgressShipmentsList.size}"
                                    else -> "${completedShipmentsList.size}"
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Total value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Value: ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = currencyFormatter.format(
                                if (currentFilter.hasFilters()) {
                                    filteredTotalValue
                                } else {
                                    when (selectedTabIndex) {
                                        0 -> totalValue
                                        1 -> inProgressValue
                                        else -> completedValue
                                    }
                                }
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Display the appropriate shipment list
            val displayList = if (currentFilter.hasFilters()) {
                filteredShipments
            } else {
                when (selectedTabIndex) {
                    0 -> allShipmentsList
                    1 -> inProgressShipmentsList
                    else -> completedShipmentsList
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (displayList.isEmpty()) {
                    Text(
                        text = if (currentFilter.hasFilters()) {
                            "No shipments match the current filters."
                        } else {
                            "No shipments in this category."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                        items(displayList) { shipment ->
                            ShipmentRow(
                                shipment = shipment,
                                onEditClick = onEditShipmentClick
                            )
                        }
                    }
                }
            }
        }
    }
} 