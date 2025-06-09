package com.example.rentease.ui.helpers

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Helper class for handling window insets in edge-to-edge display mode.
 * Provides consistent inset handling across fragments.
 */
object WindowInsetsHelper {

    /**
     * Apply window insets to a root view and app bar layout for proper edge-to-edge display.
     * This ensures the content is properly positioned below the status bar and above the navigation bar.
     *
     * @param rootView The root view of the fragment/activity
     * @param appBarLayout The app bar layout that should extend into the status bar area (can be null)
     */
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

    /**
     * Apply window insets to a root view without an app bar layout.
     * This is useful for fragments that don't have a toolbar.
     *
     * @param rootView The root view of the fragment/activity
     */
    fun applyWindowInsets(rootView: View) {
        applyWindowInsets(rootView, null)
    }
}
