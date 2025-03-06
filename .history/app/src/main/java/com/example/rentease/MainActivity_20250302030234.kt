package com.example.rentease

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController

/**
 * MainActivity serves as the single activity container for all fragments in the app.
 * It handles navigation between fragments using the Navigation Component.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Set up ActionBar with NavController
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.propertyListFragment) // Set propertyListFragment as the top-level destination
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Handle navigation from intent
        handleNavigationIntent(intent)
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
                // "contact_form" handling removed as it's replaced by request_form
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
                "full_screen_image" -> {
                    val imageUrls = intent.getStringArrayListExtra("image_urls")
                    val position = intent.getIntExtra("position", 0)
                    if (imageUrls != null) {
                        val bundle = Bundle().apply {
                            putStringArray("imageUrls", imageUrls.toTypedArray())
                            putInt("position", position)
                        }
                        navController.navigate(R.id.fullScreenImageFragment, bundle)
                    }
                }
                // Add other destinations as needed
            }
        }
        
        // Handle property details navigation (direct property_id extra)
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
