# RentEase Fragment Architecture

This document explains the fragment-based architecture used in the RentEase application.

## Overview

RentEase is transitioning from a multi-activity architecture to a single-activity architecture using fragments. This approach offers several benefits:

- **Modularity**: UI components are self-contained in fragments
- **Reusability**: Fragments can be reused across different screens
- **Lifecycle Management**: Fragment lifecycle is properly managed
- **Navigation**: Simplified navigation with the Navigation Component
- **Maintainability**: Clearer separation of concerns

## Architecture Components

### 1. Base Fragment

All fragments in the application extend `BaseFragment<T>` which provides common functionality:

```kotlin
abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    // Common functionality for all fragments
}
```

The base fragment handles:
- ViewBinding lifecycle
- Common UI setup
- Observer setup

### 2. Navigation

Navigation between fragments is handled by the Navigation Component:

- **Navigation Graph**: Defined in `nav_graph.xml`
- **NavController**: Used to navigate between fragments
- **NavigationHelper**: Utility class for navigation

### 3. Single Activity

`MainActivity` serves as the container for all fragments:

- Hosts the NavHostFragment
- Handles deep linking
- Manages the ActionBar

## Fragment Structure

Each screen in the application is represented by a fragment:

- **PropertyListFragment**: Displays the list of properties
- **PropertyDetailsFragment**: Shows detailed information about a property
- **LoginFragment**: Handles user authentication
- etc.

## How to Use

### Creating a New Screen

1. Create a fragment layout XML file:
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <!-- UI components -->
</androidx.constraintlayout.widget.ConstraintLayout>
```

2. Create a fragment class extending BaseFragment:
```kotlin
class YourFragment : BaseFragment<FragmentYourBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentYourBinding {
        return FragmentYourBinding.inflate(inflater, container, false)
    }

    override fun setupUI() {
        // Initialize UI components
    }

    override fun setupObservers() {
        // Set up observers for LiveData or Flow
    }
}
```

3. Add the fragment to the navigation graph:
```xml
<fragment
    android:id="@+id/yourFragment"
    android:name="com.example.rentease.ui.your.YourFragment"
    android:label="Your Screen"
    tools:layout="@layout/fragment_your">
    <!-- Define actions and arguments -->
</fragment>
```

### Navigating Between Fragments

Use the Navigation Component to navigate between fragments:

```kotlin
// Using an action
findNavController().navigate(R.id.action_currentFragment_to_destinationFragment)

// Using an action with arguments
val action = CurrentFragmentDirections.actionCurrentFragmentToDestinationFragment(arg1, arg2)
findNavController().navigate(action)

// Using the NavigationHelper
NavigationHelper.navigate(
    findNavController(),
    R.id.destinationFragment,
    bundleOf("key" to value)
)
```

## Transition Strategy

The application is in the process of transitioning from a multi-activity architecture to a single-activity architecture. During this transition:

1. Activities are being simplified to be just containers for fragments
2. UI logic is being moved from activities to fragments
3. Navigation is being updated to use the Navigation Component

## Naming Conventions

- Fragment classes: `FeatureFragment` (e.g., `PropertyListFragment`)
- Fragment layouts: `fragment_feature.xml` (e.g., `fragment_property_list.xml`)
- Navigation actions: `action_sourceFragment_to_destinationFragment`

## Best Practices

1. Keep fragments focused on a single responsibility
2. Use ViewModels to share data between fragments
3. Use the Navigation Component for all navigation
4. Handle configuration changes properly
5. Use the NavigationHelper for complex navigation scenarios
