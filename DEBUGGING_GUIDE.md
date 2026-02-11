# Debugging Guide - Owner Details Navigation Issue

## Problem
App exits after clicking Continue on Owner Details screen, then shows "Redirecting to vehicle_details" followed by navigation failure.

## Changes Made

### 1. Enhanced Fragment Lifecycle Management
- Changed coroutine from `lifecycleScope` to `viewLifecycleOwner.lifecycleScope`
- Added `isAdded` and `isDetached` checks before all navigation attempts
- Added checks after each async operation to ensure fragment is still alive

### 2. Safe Navigation Implementation
- Added 300ms delay before navigation using `view?.postDelayed()`
- Wrapped all navigation in try-catch with `IllegalStateException` handling
- Added comprehensive lifecycle state validation before navigating

### 3. Comprehensive Logging
All critical points now have detailed logging with tags:
- `[LIFECYCLE]` - Fragment/Activity lifecycle events
- `[FLOW]` - Step-by-step flow progression
- `[API]` - API calls and responses
- `[UPLOAD]` - S3 upload operations
- `[NAVIGATION]` - Navigation attempts and results
- `[ERROR]` - Error cases

## How to Debug

### Option 1: Using Android Studio Logcat
1. Open Android Studio
2. Go to `View → Tool Windows → Logcat`
3. Filter by tag: `MainActivity|OwnerDetailsFragment|OnboardingViewModel|AppStateViewModel`
4. Run the app and perform the action
5. Review the logs in order

### Option 2: Using Terminal (Recommended)
1. Connect your device/emulator
2. Run the log capture script:
   ```bash
   ./capture_logs.sh
   ```
3. Run the app and click Continue on Owner Details
4. Press Ctrl+C to stop logging
5. Review the `debug_logs.txt` file

### Option 3: Manual Command
```bash
adb logcat -s MainActivity:D OwnerDetailsFragment:D OnboardingViewModel:D AppStateViewModel:D
```

## What to Look For in Logs

### Successful Flow Should Show:
```
[USER ACTION] Continue button clicked
[VALIDATION] Form validation passed
[UPLOAD] Starting upload process...
[UPLOAD] S3 upload completed successfully
[API] saveOwner() called
[API] Response received
[API] Setting ownerSaveSuccess to TRUE
[OBSERVER] ownerSaveSuccess triggered: true
[FLOW] Step 1: Owner details saved successfully
[FLOW] Step 2: Fetching app state...
[API] getAppState() called
[API] Response received
[API] Setting nextScreen to: vehicle
[OBSERVER] nextScreen triggered: 'vehicle'
[FLOW] Step 4: Received next screen: vehicle
[NAVIGATION] Fragment state - isAdded: true, isDetached: false
[NAVIGATION] Executing navigation after delay...
[NAVIGATION] Attempting to navigate to VehicleDetailsFragment...
[NAVIGATION] ✓ Navigation to vehicle details SUCCESSFUL
```

### If Navigation Fails, Look For:
- Fragment lifecycle states (isAdded, isDetached, isVisible)
- Activity lifecycle events (onPause, onStop, onDestroy)
- NavController errors
- IllegalStateException or other exceptions

## Common Failure Patterns

### Pattern 1: Fragment Destroyed Before Navigation
```
[OBSERVER] nextScreen triggered: 'vehicle'
[NAVIGATION] Fragment not in valid state - isAdded: false
```
**Solution**: Fragment is being destroyed - check MainActivity lifecycle

### Pattern 2: NavController Exception
```
[NAVIGATION] Attempting to navigate to VehicleDetailsFragment...
[NAVIGATION] ✗ IllegalStateException
```
**Solution**: Navigation action missing or invalid - check auth_navigation.xml

### Pattern 3: API Not Returning nextScreen
```
[API] Response data is NULL
```
**Solution**: Backend not returning proper response - check API

## Files Modified
1. `OwnerDetailsFragment.kt` - Added lifecycle checks and safe navigation
2. `OnboardingViewModel.kt` - Added comprehensive API call logging
3. `AppStateViewModel.kt` - Added app state response logging
4. `MainActivity.kt` - Added activity lifecycle logging
5. `auth_navigation.xml` - Added missing navigation actions

## Next Steps
1. Run the app with logging enabled
2. Capture the complete log output
3. Share the logs to identify the exact failure point
4. Apply targeted fix based on the specific issue found
