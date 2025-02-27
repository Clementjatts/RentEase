# RentEase Fragment Migration Cleanup Plan

This document outlines the plan for cleaning up redundant files after migrating to a fragment-based architecture.

## Phase 1: Mark Deprecated Files

### Activity Classes to Mark as Deprecated

Add `@Deprecated` annotations to these activity classes:

```kotlin
@Deprecated("Use MainActivity with PropertyListFragment instead")
class PropertyListActivity : AppCompatActivity() { ... }
```

Activity classes to mark:
- `PropertyListActivity.kt`
- `PropertyDetailsActivity.kt`
- `LoginActivity.kt`
- `RegisterActivity.kt`
- `ProfileActivity.kt`
- `PropertyFormActivity.kt`
- `ContactFormActivity.kt`
- `RequestFormActivity.kt`
- `PropertyManagementActivity.kt`
- `AdminDashboardActivity.kt`
- `LandlordDashboardActivity.kt`
- `UserManagementActivity.kt`

### Layout Files to Mark for Removal

Add comments to these layout files indicating they'll be removed:

```xml
<!-- 
  This layout file is deprecated and will be removed in a future update.
  Use fragment_property_list.xml instead.
-->
```

Layout files to mark:
- Original complex versions of:
  - `activity_property_list.xml`
  - `activity_property_details.xml`
  - `activity_login.xml`
  - `activity_register.xml`
  - `activity_profile.xml`
  - `activity_property_form.xml`
  - `activity_contact_form.xml`
  - `activity_request_form.xml`
  - `activity_property_management.xml`
  - `activity_admin_dashboard.xml`
  - `activity_landlord_dashboard.xml`
  - `activity_user_management.xml`

## Phase 2: Create Fragment Equivalents

For each activity, create a fragment equivalent if not already created:

1. Create fragment layout XML file
2. Create fragment class extending BaseFragment
3. Add fragment to navigation graph
4. Update navigation to use the fragment

## Phase 3: Update Navigation

1. Replace direct activity starts with Navigation Component navigation
2. Use NavigationHelper for complex navigation scenarios
3. Update deep links to point to MainActivity

## Phase 4: Remove Redundant Files

Once the migration is complete, these files can be removed:

### Activity Classes to Remove
- All activity classes except MainActivity

### Layout Files to Remove or Consolidate
- All complex activity layouts (keep simplified container layouts during transition)

### Update AndroidManifest.xml
- Remove activity declarations except MainActivity
- Update intent filters to point to MainActivity
- Configure deep linking for MainActivity

## Testing Strategy

Before removing any files:
1. Test all navigation flows using fragments
2. Verify proper back stack behavior
3. Test configuration changes (rotation, etc.)
4. Test deep linking

## Timeline

- Phase 1: Immediate
- Phase 2: 1-2 weeks
- Phase 3: 2-3 weeks
- Phase 4: After thorough testing (3-4 weeks)
