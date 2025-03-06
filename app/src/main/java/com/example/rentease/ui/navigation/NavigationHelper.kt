package com.example.rentease.ui.navigation

import android.os.Bundle
import androidx.navigation.NavController
import com.example.rentease.R
import com.example.rentease.auth.UserType

/**
 * NavigationHelper provides navigation methods for fragment-based navigation.
 * This class centralizes all navigation logic for the single-activity architecture.
 * It uses NavController for all navigation operations.
 */
object NavigationHelper {
    
    // Login Screen
    
    fun navigateToLogin(navController: NavController) {
        navController.navigate(R.id.action_global_loginFragment)
    }
    
    // Register Screen
    
    fun navigateToRegister(navController: NavController) {
        navController.navigate(R.id.action_loginFragment_to_registerFragment)
    }
    
    // Property List Screen
    
    fun navigateToPropertyList(navController: NavController) {
        navController.navigate(R.id.action_global_propertyListFragment)
    }
    
    // Property Details Screen
    
    fun navigateToPropertyDetails(navController: NavController, propertyId: Int) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
        }
        navController.navigate(R.id.action_propertyListFragment_to_propertyDetailsFragment, bundle)
    }
    
    // Profile Screen
    
    fun navigateToProfile(navController: NavController) {
        navController.navigate(R.id.action_global_profileFragment)
    }
    
    // Property Form Screen
    
    fun navigateToPropertyForm(navController: NavController, propertyId: Int = -1) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
        }
        navController.navigate(R.id.action_global_propertyFormFragment, bundle)
    }

    // Dashboard Navigation
    
    fun navigateToDashboard(navController: NavController, userType: UserType?) {
        when (userType) {
            UserType.ADMIN -> navigateToAdminDashboard(navController)
            UserType.LANDLORD -> navigateToLandlordDashboard(navController)
            null -> navigateToPropertyList(navController) // Non-logged in users go to property list
        }
    }
    
    // Landlord Dashboard Screen
    
    private fun navigateToLandlordDashboard(navController: NavController) {
        navController.navigate(R.id.action_global_landlordDashboardFragment)
    }
    
    // Admin Dashboard Screen
    
    private fun navigateToAdminDashboard(navController: NavController) {
        navController.navigate(R.id.action_global_adminDashboardFragment)
    }
    
    // User Management Screen
    
    fun navigateToUserManagement(navController: NavController) {
        navController.navigate(R.id.action_adminDashboardFragment_to_userManagementFragment)
    }
    
    // Property Management Screen
    
    fun navigateToPropertyManagement(navController: NavController) {
        navController.navigate(R.id.action_landlordDashboardFragment_to_propertyManagementFragment)
    }
    
    // Request Form Screen
    
    fun navigateToRequestForm(navController: NavController, propertyId: Int, landlordId: Int) {
        val bundle = Bundle().apply {
            putInt("propertyId", propertyId)
            putInt("landlordId", landlordId)
        }
        // Use action_propertyDetailsFragment_to_requestFormFragment if coming from property details
        if (navController.currentDestination?.id == R.id.propertyDetailsFragment) {
            navController.navigate(R.id.action_propertyDetailsFragment_to_requestFormFragment, bundle)
        } else {
            // Otherwise navigate directly to the fragment
            navController.navigate(R.id.requestFormFragment, bundle)
        }
    }
    
    // Full Screen Image Screen
    
    fun navigateToFullScreenImage(navController: NavController, imageUrls: Array<String>, position: Int) {
        val bundle = Bundle().apply {
            putStringArray("imageUrls", imageUrls)
            putInt("position", position)
        }
        navController.navigate(R.id.fullScreenImageFragment, bundle)
    }
}
