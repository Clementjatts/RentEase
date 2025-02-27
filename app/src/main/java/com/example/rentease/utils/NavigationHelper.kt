package com.example.rentease.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.rentease.MainActivity
import com.example.rentease.R

/**
 * NavigationHelper provides utility methods for navigating between screens in the app.
 * It supports both the legacy multi-activity approach and the new single-activity architecture.
 * 
 * This class helps with the transition from multiple activities to a single activity with fragments.
 */
object NavigationHelper {
    
    /**
     * Navigate to the login screen.
     * 
     * @param context The context to use for navigation
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToLogin(context: Context, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            navigateToMainActivity(context, "login")
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(LoginActivity.createIntent(context))
        }
    }
    
    /**
     * Navigate to the property details screen.
     * 
     * @param context The context to use for navigation
     * @param propertyId The ID of the property to display
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToPropertyDetails(context: Context, propertyId: Int, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("property_id", propertyId)
            }
            context.startActivity(intent)
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(PropertyDetailsActivity.createIntent(context, propertyId))
        }
    }
    
    /**
     * Navigate to the property list screen.
     * 
     * @param context The context to use for navigation
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToPropertyList(context: Context, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            navigateToMainActivity(context, null)
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(PropertyListActivity.createIntent(context))
        }
    }
    
    /**
     * Navigate to the profile screen.
     * 
     * @param context The context to use for navigation
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToProfile(context: Context, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            navigateToMainActivity(context, "profile")
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(ProfileActivity.createIntent(context))
        }
    }
    
    /**
     * Navigate to the property form screen.
     * 
     * @param context The context to use for navigation
     * @param propertyId The ID of the property to edit, or -1 to create a new property
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToPropertyForm(context: Context, propertyId: Int = -1, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "property_form")
                putExtra("property_id", propertyId)
            }
            context.startActivity(intent)
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(PropertyFormActivity.createIntent(context, propertyId))
        }
    }
    
    /**
     * Navigate to the contact form screen.
     * 
     * @param context The context to use for navigation
     * @param propertyId The ID of the property to contact about
     * @param useFragmentNavigation Whether to use fragment navigation (true) or activity navigation (false)
     */
    fun navigateToContactForm(context: Context, propertyId: Int, useFragmentNavigation: Boolean = true) {
        if (useFragmentNavigation) {
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("navigate_to", "contact_form")
                putExtra("property_id", propertyId)
            }
            context.startActivity(intent)
        } else {
            // Legacy approach - will be removed once migration is complete
            // context.startActivity(ContactFormActivity.createIntent(context, propertyId))
        }
    }
    
    /**
     * Navigate directly using the NavController.
     * This method should be used when navigating between fragments within the same activity.
     * 
     * @param navController The NavController to use for navigation
     * @param destinationId The ID of the destination to navigate to
     * @param args Optional arguments to pass to the destination
     * @param navOptions Optional navigation options
     */
    fun navigate(
        navController: NavController,
        destinationId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null
    ) {
        navController.navigate(destinationId, args, navOptions)
    }
    
    /**
     * Navigate to the property details screen using the NavController.
     * 
     * @param navController The NavController to use for navigation
     * @param propertyId The ID of the property to display
     */
    fun navigateToPropertyDetails(navController: NavController, propertyId: Int) {
        val args = bundleOf("propertyId" to propertyId)
        navController.navigate(R.id.propertyDetailsFragment, args)
    }
    
    /**
     * Navigate to the property form screen using the NavController.
     * 
     * @param navController The NavController to use for navigation
     * @param propertyId The ID of the property to edit, or -1 to create a new property
     */
    fun navigateToPropertyForm(navController: NavController, propertyId: Int = -1) {
        val args = bundleOf("propertyId" to propertyId)
        navController.navigate(R.id.propertyFormFragment, args)
    }
    
    /**
     * Navigate to the contact form screen using the NavController.
     * 
     * @param navController The NavController to use for navigation
     * @param propertyId The ID of the property to contact about
     */
    fun navigateToContactForm(navController: NavController, propertyId: Int) {
        val args = bundleOf("propertyId" to propertyId)
        navController.navigate(R.id.contactFormFragment, args)
    }
    
    /**
     * Helper method to navigate to MainActivity with a specific destination.
     * 
     * @param context The context to use for navigation
     * @param destination The destination to navigate to, or null to go to the default destination
     */
    private fun navigateToMainActivity(context: Context, destination: String?) {
        val intent = Intent(context, MainActivity::class.java).apply {
            if (destination != null) {
                putExtra("navigate_to", destination)
            }
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }
}
