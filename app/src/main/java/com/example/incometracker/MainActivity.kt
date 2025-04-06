package com.example.incometracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // Import for viewModels delegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.incometracker.ui.screen.AddShipmentScreen
import com.example.incometracker.ui.screen.EditShipmentScreen
import com.example.incometracker.ui.screen.MonthlyDetailScreen
import com.example.incometracker.ui.screen.MonthlyReportScreen
import com.example.incometracker.ui.screen.ProductPriceSettingsScreen
import com.example.incometracker.ui.screen.ShipmentListScreen
import com.example.incometracker.ui.screen.MonthlySummaryScreen
import com.example.incometracker.ui.theme.IncomeTrackerTheme
import com.example.incometracker.ui.viewmodel.ShipmentViewModel

class MainActivity : ComponentActivity() {

    // Instantiate ViewModel using activity-ktx delegate
    private val shipmentViewModel: ShipmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IncomeTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(shipmentViewModel)
                }
            }
        }
    }
}

// Define navigation routes
object AppDestinations {
    const val SHIPMENT_LIST = "shipmentList"
    const val ADD_SHIPMENT = "addShipment"
    const val EDIT_SHIPMENT = "editShipment/{shipmentId}"
    const val PRODUCT_PRICE_SETTINGS = "productPriceSettings"
    const val MONTHLY_REPORT = "monthlyReport"
    const val MONTHLY_DETAIL = "monthlyDetail/{monthYear}"
    const val MONTHLY_SUMMARY = "monthlySummary"
    
    // Helper function to create edit shipment route with ID
    fun editShipmentRoute(shipmentId: Int) = "editShipment/$shipmentId"
    
    // Helper function to create monthly detail route with month-year
    fun monthlyDetailRoute(monthYear: String) = "monthlyDetail/$monthYear"
}

@Composable
fun AppNavigation(viewModel: ShipmentViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.SHIPMENT_LIST) {
        composable(AppDestinations.SHIPMENT_LIST) {
            ShipmentListScreen(
                viewModel = viewModel,
                onAddShipmentClick = { navController.navigate(AppDestinations.ADD_SHIPMENT) }, // Navigate to Add screen
                onSettingsClick = { navController.navigate(AppDestinations.PRODUCT_PRICE_SETTINGS) },
                onEditShipmentClick = { shipmentId -> 
                    navController.navigate(AppDestinations.editShipmentRoute(shipmentId))
                },
                onMonthlyReportClick = { navController.navigate(AppDestinations.MONTHLY_REPORT) }
            )
        }
        composable(AppDestinations.ADD_SHIPMENT) {
            AddShipmentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() } // Navigate back to List screen
            )
        }
        composable(
            route = AppDestinations.EDIT_SHIPMENT,
            arguments = listOf(
                navArgument("shipmentId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val shipmentId = backStackEntry.arguments?.getInt("shipmentId") ?: 0
            EditShipmentScreen(
                shipmentId = shipmentId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.PRODUCT_PRICE_SETTINGS) {
            ProductPriceSettingsScreen(
                onNavigateBack = { navController.popBackStack() } // Navigate back to List screen
            )
        }
        composable(AppDestinations.MONTHLY_REPORT) {
            MonthlyReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onMonthSelected = { monthYear ->
                    navController.navigate(AppDestinations.monthlyDetailRoute(monthYear))
                }
            )
        }
        composable(AppDestinations.MONTHLY_SUMMARY) {
            MonthlySummaryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.MONTHLY_DETAIL,
            arguments = listOf(
                navArgument("monthYear") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val monthYear = backStackEntry.arguments?.getString("monthYear") ?: ""
            MonthlyDetailScreen(
                monthYear = monthYear,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditShipmentClick = { shipmentId ->
                    navController.navigate(AppDestinations.editShipmentRoute(shipmentId))
                }
            )
        }
    }
} 