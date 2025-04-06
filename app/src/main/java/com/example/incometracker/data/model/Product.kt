package com.example.incometracker.data.model

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Product(val displayName: String, defaultPrice: Double) {
    FISH_SKIN_SALTED_EGG("Fish Skin Salted Egg", 50000.0),
    FISH_SKIN_ORIGINAL("Fish Skin Original", 45000.0),
    FISH_SKIN_ORIGINAL_PLASTIC("Fish Skin Original (Plastic)", 40000.0);
    
    // Private backing fields for our price state flows
    private val _priceFlow = MutableStateFlow(defaultPrice)
    
    // Public read-only state flow to observe price changes
    val priceFlow: StateFlow<Double> = _priceFlow.asStateFlow()
    
    // Current price property with getter
    val price: Double
        get() = _priceFlow.value
    
    // Update price method
    fun updatePrice(newPrice: Double) {
        if (newPrice > 0) {
            _priceFlow.value = newPrice
        }
    }
    
    companion object {
        // Initialize from saved preferences if available
        fun initializeFromPreferences(context: android.content.Context) {
            val sharedPrefs = context.getSharedPreferences("product_prices", android.content.Context.MODE_PRIVATE)
            values().forEach { product -> 
                val savedPrice = sharedPrefs.getFloat(product.name, product.price.toFloat())
                product.updatePrice(savedPrice.toDouble())
            }
        }
        
        // Save current prices to preferences
        fun savePricesToPreferences(context: android.content.Context) {
            val sharedPrefs = context.getSharedPreferences("product_prices", android.content.Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            values().forEach { product ->
                editor.putFloat(product.name, product.price.toFloat())
            }
            editor.apply()
        }
    }
} 