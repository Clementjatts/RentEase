package com.example.rentease

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

/**
 * MainActivity serves as the single activity container for all fragments in the app.
 * It handles navigation between fragments using the Navigation Component.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Handle navigation from intent
        handleNavigationIntent(intent)
    }

    /**
     * Enable edge-to-edge display for Android 15 compatibility
     */
    private fun enableEdgeToEdge() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Don't apply padding to the root view - let fragments handle their own insets
        // This allows for proper edge-to-edge display with toolbars extending into status bar area
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent) {
        // Handle navigation from string extra
        intent.getStringExtra("navigate_to")?.let { destination ->
            when (destination) {
                "login" -> navController.navigate(R.id.loginFragment)
                "register" -> navController.navigate(R.id.registerFragment)
                "profile" -> navController.navigate(R.id.profileFragment)
                "property_form" -> {
                    val propertyId = intent.getIntExtra("property_id", -1)
                    val bundle = Bundle().apply {
                        putInt("propertyId", propertyId)
                    }
                    navController.navigate(R.id.propertyFormFragment, bundle)
                }
                "request_form" -> {
                    val propertyId = intent.getIntExtra("property_id", -1)
                    val landlordId = intent.getIntExtra("landlord_id", 0)
                    if (propertyId != -1) {
                        val bundle = Bundle().apply {
                            putInt("propertyId", propertyId)
                            putInt("landlordId", landlordId)
                        }
                        navController.navigate(R.id.requestFormFragment, bundle)
                    }
                }

            }
        }

        if (intent.getStringExtra("navigate_to") == null) {
            intent.getIntExtra("property_id", -1).takeIf { it != -1 }?.let { propertyId ->
                val bundle = Bundle().apply {
                    putInt("propertyId", propertyId)
                }
                navController.navigate(R.id.propertyDetailsFragment, bundle)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
