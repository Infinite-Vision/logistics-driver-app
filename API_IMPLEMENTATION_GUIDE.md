# API Integration Implementation Guide

## Overview
This document describes the complete API integration based on the Backend Specification v1.

## Changes Implemented

### 1. API Models (ApiModels.kt)
Created comprehensive data models matching the backend API specification:

#### Response Wrappers
- `ApiResponse<T>` - Standard wrapper for all API responses
- `MessageResponse` - Simple message responses

#### Authentication Models
- `LogoutResponse` - Logout API response

#### App State Models
- `AppStateResponse` - User's onboarding progress and next screen
- `UpdateLanguageRequest/Response` - Language preference updates

#### Onboarding Models
- **Owner**: `SaveOwnerRequest`, `OwnerResponse`
- **Vehicle**: `SaveVehicleRequest`, `VehicleResponse`
- **Driver**: `SaveDriverRequest`, `DriverResponse`

#### Meta Models
- `FormOption` - Dropdown options
- `VehicleFormOptionsResponse` - Vehicle form dropdown data

#### Enums
- `LanguageCode`: EN, TA, HI
- `VehicleType`: TRUCK, MINI_TRUCK, THREE_WHEELER, PICKUP
- `BodyType`: OPEN, CLOSED, SEMI_OPEN
- `BodySpec`: EIGHT_FT_1_5_TON, FOURTEEN_FT_3_5_TON, SEVENTEEN_FT_4_5_TON, NINETEEN_FT_6_TON
- `OnboardingStatus`, `OnboardingStep`, `DriverStatus`, `VerificationStatus`

### 2. API Service (ApiService.kt)
Updated with all backend endpoints:

#### Authentication Module
- `POST /auth/otp/request` - Request OTP
- `POST /auth/otp/verify` - Verify OTP and get JWT
- `POST /auth/logout` - Logout user

#### App State Module
- `GET /app/state` - Get user's app state and next screen

#### User Preferences Module
- `POST /users/language` - Update preferred language

#### Onboarding Owner Module
- `POST /onboarding/owner` - Save owner details
- `GET /onboarding/owner` - Get owner details

#### Onboarding Vehicle Module
- `POST /onboarding/vehicle` - Save vehicle details
- `GET /onboarding/vehicle` - Get vehicle details

#### Onboarding Driver Module
- `POST /onboarding/driver` - Save driver details
- `GET /onboarding/driver` - Get driver details

#### Meta Module
- `GET /meta/vehicle-form-options` - Get form dropdowns (localized)

### 3. ViewModels

#### PhoneViewModel.kt
- Fixed OTP request error handling
- Updated to handle new API response structure
- Better error message extraction from `ApiResponse.message` field

#### OTPViewModel.kt
- Fixed OTP verification error handling
- Updated resend OTP logic
- Improved error message extraction

#### OnboardingViewModel.kt (NEW)
Comprehensive ViewModel for all onboarding steps:
- **Owner Methods**: `saveOwner()`, `getOwner()`
- **Vehicle Methods**: `saveVehicle()`, `getVehicle()`, `getVehicleFormOptions()`
- **Driver Methods**: `saveDriver()`, `getDriver()`
- Automatic token management using SharedPreference
- HTTP error code handling (400, 401, 403, 429, 500)
- Network error handling

#### AppStateViewModel.kt (NEW)
Manages app flow and user preferences:
- `getAppState()` - Fetches user progress and next screen
- `updateLanguage()` - Updates user's preferred language
- `logout()` - Logout with session cleanup
- Automatic session management

### 4. Networking Layer

#### RetrofitClient.kt
- Updated to support AuthInterceptor
- Added context initialization for automatic token injection
- Improved timeout configuration (30 seconds)
- Logging interceptor for debugging

#### AuthInterceptor.kt (NEW)
- Automatically adds JWT token to authenticated requests
- Reads token from SharedPreference
- Adds "Bearer <token>" to Authorization header

## Usage Guide

### 1. Initialize RetrofitClient
In your Application class:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }
}
```

### 2. Request OTP Flow
```kotlin
class PhoneActivity : AppCompatActivity() {
    private val viewModel: PhoneViewModel by viewModels()
    
    fun requestOTP() {
        viewModel.sendOTP("+91", "9876543210")
        
        viewModel.otpSent.observe(this) { success ->
            if (success) {
                // Navigate to OTP screen
            }
        }
        
        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        }
    }
}
```

### 3. Verify OTP Flow
```kotlin
class OTPActivity : AppCompatActivity() {
    private val viewModel: OTPViewModel by viewModels()
    
    fun verifyOTP() {
        viewModel.verifyOTP("+91", "9876543210", "1234", "EN")
        
        viewModel.verificationSuccess.observe(this) { success ->
            if (success) {
                // Check onboarding status
                checkAppState()
            }
        }
        
        viewModel.onboardingStatus.observe(this) { status ->
            // Handle onboarding status
        }
        
        viewModel.onboardingStep.observe(this) { step ->
            // Navigate to appropriate screen based on step
        }
    }
}
```

### 4. Check App State
```kotlin
class SplashActivity : AppCompatActivity() {
    private val viewModel: AppStateViewModel by viewModels()
    
    fun checkAppState() {
        viewModel.getAppState()
        
        viewModel.appState.observe(this) { state ->
            when (state.nextScreen) {
                "owner_details" -> navigateToOwnerScreen()
                "vehicle_details" -> navigateToVehicleScreen()
                "driver_details" -> navigateToDriverScreen()
                "home" -> navigateToHome()
            }
        }
    }
}
```

### 5. Onboarding - Owner
```kotlin
class OwnerDetailsActivity : AppCompatActivity() {
    private val viewModel: OnboardingViewModel by viewModels()
    
    fun saveOwnerDetails() {
        viewModel.saveOwner(
            name = "John Doe",
            ownerSelfieUrl = "https://...",
            ownerAdharUrl = "https://...",
            ownerPanUrl = "https://..."
        )
        
        viewModel.ownerSaveSuccess.observe(this) { success ->
            if (success) {
                // Navigate to vehicle details
            }
        }
        
        viewModel.ownerData.observe(this) { owner ->
            // Owner saved with ID: owner.id
        }
    }
}
```

### 6. Onboarding - Vehicle
```kotlin
class VehicleDetailsActivity : AppCompatActivity() {
    private val viewModel: OnboardingViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load form options first
        viewModel.getVehicleFormOptions()
        
        viewModel.vehicleFormOptions.observe(this) { options ->
            // Populate dropdowns with:
            // - options.vehicleTypes
            // - options.bodyTypes
            // - options.bodySpecs
        }
    }
    
    fun saveVehicleDetails() {
        viewModel.saveVehicle(
            registrationNumber = "TN23AB1234",
            vehicleType = "TRUCK",
            bodyType = "OPEN",
            bodySpec = "EIGHT_FT_1_5_TON",
            rcUrl = "https://...",
            insuranceUrl = "https://...",
            pucUrl = "https://..."
        )
        
        viewModel.vehicleSaveSuccess.observe(this) { success ->
            if (success) {
                // Navigate to driver details
            }
        }
    }
}
```

### 7. Onboarding - Driver
```kotlin
class DriverDetailsActivity : AppCompatActivity() {
    private val viewModel: OnboardingViewModel by viewModels()
    
    fun saveDriverDetails() {
        // For self-driving
        viewModel.saveDriver(
            isSelfDriving = true,
            name = null,
            phoneNumber = null,
            driverLicenseUrl = "https://..."
        )
        
        // OR for separate driver
        viewModel.saveDriver(
            isSelfDriving = false,
            name = "Driver Name",
            phoneNumber = "9876543210",
            driverLicenseUrl = "https://..."
        )
        
        viewModel.driverSaveSuccess.observe(this) { success ->
            if (success) {
                // Onboarding complete!
                // Navigate to home screen
            }
        }
        
        viewModel.driverData.observe(this) { driver ->
            // Check verification status
            when (driver.verificationStatus) {
                "IN_PROGRESS" -> showPendingVerification()
                "VERIFIED" -> proceedToHome()
                "REJECTED" -> showRejectionReason()
            }
        }
    }
}
```

### 8. Update Language
```kotlin
class SettingsActivity : AppCompatActivity() {
    private val viewModel: AppStateViewModel by viewModels()
    
    fun updateLanguage(language: String) {
        val phoneNumber = SharedPreference.getInstance(this).getPhoneNumber()
        viewModel.updateLanguage(phoneNumber, language)
        
        viewModel.preferredLanguage.observe(this) { lang ->
            // Language updated successfully
            // Restart app or update UI
        }
    }
}
```

### 9. Logout
```kotlin
fun logout() {
    val viewModel: AppStateViewModel by viewModels()
    viewModel.logout()
    
    // Session is automatically cleared
    // Navigate to login screen
    startActivity(Intent(this, LoginActivity::class.java))
    finish()
}
```

## Error Handling

All ViewModels provide consistent error handling:

### Common Error Codes
- **400**: Invalid data/Bad request
- **401**: Unauthorized/Session expired
- **403**: Access denied
- **429**: Too many requests
- **500**: Service unavailable
- **IOException**: Network error

### Observing Errors
```kotlin
viewModel.errorMessage.observe(this) { error ->
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
}

viewModel.loading.observe(this) { isLoading ->
    progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
}
```

## Backend API Response Structure

All APIs return responses in this format:

### Success Response
```json
{
  "success": true,
  "data": {
    // Response data here
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE"
}
```

## Important Notes

1. **JWT Token Management**: Tokens are automatically stored in SharedPreference after OTP verification

2. **Authorization Headers**: All authenticated endpoints automatically get "Bearer <token>" header

3. **Language Codes**: Must use uppercase enum values (EN, TA, HI) when sending to backend

4. **Vehicle Type Enums**: Send uppercase enum values (TRUCK, MINI_TRUCK, etc.) to backend

5. **Conditional Driver Logic**: 
   - If `isSelfDriving = true`: name and phoneNumber are NOT required
   - If `isSelfDriving = false`: name and phoneNumber are REQUIRED

6. **Onboarding Flow**:
   - Owner → Vehicle → Driver → Submitted
   - Check `onboardingStep` to determine current position

7. **Network Timeouts**: All requests have 30-second timeout

## Testing the Network Error Fix

The original "Network error" issue was fixed by:

1. Updated response handling to check both `response.body()?.message` and `response.body()?.data?.message`
2. Proper error code handling for HTTP exceptions
3. Consistent IOException handling for network errors
4. Better error message extraction from API responses

To test:
```kotlin
// This should now work without network errors
phoneViewModel.sendOTP("+91", "9876543210")

// Watch the logs for API call details (HttpLoggingInterceptor)
```

## Troubleshooting

### Issue: "Network error. Please check your connection."
- **Cause**: IOException during API call
- **Fix**: Check internet connection, verify backend URL is accessible
- **Logs**: Check Logcat for Retrofit logs with "OkHttp" tag

### Issue: "Service temporarily unavailable"
- **Cause**: Backend returning 500 error
- **Fix**: Wait and retry, backend might be down
- **Action**: Contact backend team if persists

### Issue: "Session expired. Please login again."
- **Cause**: JWT token expired or invalid (401 error)
- **Fix**: Clear session and navigate to login screen
- **Code**: ViewModels automatically clear session on 401

### Issue: Build errors after implementation
- **Solution**: 
  1. Clean and rebuild: `./gradlew clean build`
  2. Sync Gradle files in Android Studio
  3. Invalidate caches and restart
  4. Ensure all dependencies are properly configured

## Next Steps

1. **Initialize RetrofitClient** in Application class
2. **Test OTP flow** end-to-end
3. **Implement UI** for all onboarding screens
4. **Add form validation** before API calls
5. **Implement file upload** for document URLs
6. **Add retry mechanism** for failed requests
7. **Implement offline support** with Room database
8. **Add unit tests** for ViewModels
9. **Add integration tests** for API calls

## Support

For issues or questions:
- Review error messages in Logcat
- Check backend API documentation
- Verify network connectivity
- Ensure JWT tokens are properly stored
- Check API base URL configuration
