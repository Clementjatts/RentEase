package com.example.rentease.ui.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import com.example.rentease.MainActivity
import com.example.rentease.R
import com.example.rentease.auth.UserType

/**
 * NavigationHelper provides navigation methods for fragment-based navigation.
 * This class centralizes all navigation logic for the single-activity architecture.
 */
object NavigationHelper {
    
    // Login Screen
    
    fun navigateToLogin(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "login")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
    
    fun navigateToLogin(navController: NavController) {
        navController.navigate(R.id.action_global_loginFragment)
    }
    
    // Register Screen
    
    fun navigateToRegister(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "register")
        }
        context.startActivity(intent)
    }
    
    fun navigateToRegister(navController: NavController) {
        navController.navigate(R.id.action_loginFragment_to_registerFragment)
    }
    
    // Property List Screen
    
    fun navigateToPropertyList(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "property_list")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
    
    fun navigateToPropertyList(navController: NavController) {
        navController.navigate(R.id.action_global_propertyListFragment)
    }
    
    // Property Details Screen
    
    fun navigateToPropertyDetails(context: Context, propertyId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("property_id", propertyId)
        }
        context.startActivity(intent)
    }
    
    fun navigateToPropertyDetails(navController: NavController, propertyId: Int) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
        }
        navController.navigate(R.id.action_propertyListFragment_to_propertyDetailsFragment, bundle)
    }
    
    // Profile Screen
    
    fun navigateToProfile(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "profile")
        }
        context.startActivity(intent)
    }
    
    fun navigateToProfile(navController: NavController) {
        navController.navigate(R.id.action_global_profileFragment)
    }
    
    // Property Form Screen
    
    fun navigateToPropertyForm(context: Context, propertyId: Int = -1) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "property_form")
            putExtra("property_id", propertyId)
        }
        context.startActivity(intent)
    }
    
    fun navigateToPropertyForm(navController: NavController, propertyId: Int = -1) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
        }
        navController.navigate(R.id.action_global_propertyFormFragment, bundle)
    }
    
    // Contact Form Screen
    
    fun navigateToContactForm(context: Context, propertyId: Int = -1) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "contact_form")
            putExtra("property_id", propertyId)
        }
        context.startActivity(intent)
    }
    
    fun navigateToContactForm(navController: NavController, propertyId: Int = -1) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
        }
        navController.navigate(R.id.action_propertyDetailsFragment_to_contactFormFragment, bundle)
    }
    
    // Dashboard Navigation
    
    fun navigateToDashboard(context: Context, userType: UserType) {
        when (userType) {
            UserType.ADMIN -> navigateToAdminDashboard(context)
            UserType.LANDLORD -> navigateToLandlordDashboard(context)
            UserType.TENANT -> navigateToPropertyList(context)
        }
    }
    
    fun navigateToDashboard(navController: NavController, userType: UserType) {
        when (userType) {
            UserType.ADMIN -> navigateToAdminDashboard(navController)
            UserType.LANDLORD -> navigateToLandlordDashboard(navController)
            UserType.TENANT -> navigateToPropertyList(navController)
        }
    }
    
    // Landlord Dashboard Screen
    
    fun navigateToLandlordDashboard(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "landlord_dashboard")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
    
    fun navigateToLandlordDashboard(navController: NavController) {
        navController.navigate(R.id.action_global_landlordDashboardFragment)
    }
    
    // Admin Dashboard Screen
    
    fun navigateToAdminDashboard(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "admin_dashboard")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
    
    fun navigateToAdminDashboard(navController: NavController) {
        navController.navigate(R.id.action_global_adminDashboardFragment)
    }
    
    // User Management Screen
    
    fun navigateToUserManagement(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "user_management")
        }
        context.startActivity(intent)
    }
    
    fun navigateToUserManagement(navController: NavController) {
        navController.navigate(R.id.action_adminDashboardFragment_to_userManagementFragment)
    }
    
    // Property Management Screen
    
    fun navigateToPropertyManagement(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "property_management")
        }
        context.startActivity(intent)
    }
    
    fun navigateToPropertyManagement(navController: NavController) {
        navController.navigate(R.id.action_landlordDashboardFragment_to_propertyManagementFragment)
    }
    
    // Request Form Screen
    
    fun navigateToRequestForm(context: Context, propertyId: Int, landlordId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "request_form")
            putExtra("property_id", propertyId)
            putExtra("landlord_id", landlordId)
        }
        context.startActivity(intent)
    }
    
    fun navigateToRequestForm(navController: NavController, propertyId: Int, landlordId: Int) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
            putInt("landlordId", landlordId)
        }
        navController.navigate(R.id.requestFormFragment, bundle)
    }
    
    // Full Screen Image Screen
    
    fun navigateToFullScreenImage(context: Context, imageUrls: ArrayList<String>, position: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "full_screen_image")
            putStringArrayListExtra("image_urls", imageUrls)
            putExtra("position", position)
        }
        context.startActivity(intent)
    }
    
    fun navigateToFullScreenImage(navController: NavController, imageUrls: Array<String>, position: Int) {
        val bundle = Bundle().apply {
            putStringArray("imageUrls", imageUrls)
            putInt("position", position)
        }
        navController.navigate(R.id.fullScreenImageFragment, bundle)
    }
}
