package com.example.rentease.ui.helpers

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Helper class for handling window insets in edge-to-edge display mode
object WindowInsetsHelper {

    // Applies window insets to root view and app bar layout for proper edge-to-edge display
    fun applyWindowInsets(rootView: View, appBarLayout: View?) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply top padding to the app bar layout to account for status bar
            appBarLayout?.setPadding(
                appBarLayout.paddingLeft,
                systemBars.top,
                appBarLayout.paddingRight,
                appBarLayout.paddingBottom
            )

            // Apply bottom padding to the root view to account for navigation bar
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }

    // Applies window insets to root view without an app bar layout
    fun applyWindowInsets(rootView: View) {
        applyWindowInsets(rootView, null)
    }
}
