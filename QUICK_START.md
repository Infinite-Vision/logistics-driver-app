# Quick Start Guide for Developers

## 🚀 Running the App

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android SDK 24+
- Gradle 8.0+

### Setup Steps

1. **Open Project**
   ```
   File → Open → Select the project folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

3. **Run the App**
   - Connect Android device or start emulator
   - Click Run button (▶️)
   - Or use `Shift + F10`

## 🧪 Testing the App

### Flow 1: Language Selection
1. App opens to Language Selection screen
2. Select any language (e.g., English, Hindi)
3. Click "Continue" button
4. Should navigate to Phone Entry screen

### Flow 2: Phone Entry & OTP
1. Enter a 10-digit phone number (e.g., `9876543210`)
2. Click "Send OTP"
3. **Check Logcat** for generated OTP:
   ```
   Filter: PhoneViewModel
   Look for: "Generated OTP: XXXX for +919876543210"
   ```
4. Note the 4-digit OTP

### Flow 3: OTP Verification
1. Enter the OTP from Logcat (each digit in separate box)
2. Wait for auto-focus between boxes
3. Timer counts down from 00:30
4. Click "Verify" or wait for auto-submit
5. Should navigate to Driver Details screen

### Flow 4: Driver Details
1. Fill in the form:
   - **Name**: John Doe (required)
   - **Email**: john@example.com (optional)
   - **Address**: 123 Main Street (optional)
   - **City**: Mumbai (optional)
   - **State**: Maharashtra (optional)
   - **Pincode**: 400001 (6 digits if provided)
   - **Vehicle Number**: MH01AB1234 (format: XX00XX0000)
   - **Vehicle Type**: Car (optional)
   - **License Number**: MH1234567890 (optional)

2. Click "Submit"
3. Data saved to Room Database
4. Toast message: "Profile saved successfully"

## 📱 Testing Different States

### First Launch
```kotlin
// PreferencesManager checks:
- onboardingCompleted = false
- isLoggedIn = false
→ Starts at Language Selection
```

### After Language Selection
```kotlin
// Data in SharedPreferences:
- language = "en" (or selected language)
→ Next launch starts at Phone Entry
```

### After Login
```kotlin
// Data in SharedPreferences:
- phoneNumber = "9876543210"
- isLoggedIn = true
- sessionToken = "token_xxxxx"
→ Next launch starts at Driver Details
```

## 🔍 Database Inspection

### View Room Database
1. **Using Database Inspector** (Android Studio):
   - Run app on API 26+ device/emulator
   - Go to: View → Tool Windows → App Inspection
   - Select "Database Inspector" tab
   - Expand "logistics_driver_db"
   - View tables: `drivers`, `auth_sessions`

2. **Queries to Try**:
   ```sql
   -- View all drivers
   SELECT * FROM drivers;
   
   -- View auth sessions
   SELECT * FROM auth_sessions;
   
   -- Find driver by phone
   SELECT * FROM drivers WHERE phoneNumber = '9876543210';
   ```

### View SharedPreferences
```bash
# Using ADB
adb shell run-as com.example.logistics_driver_app

# Navigate to shared prefs
cd shared_prefs
cat logistics_driver_prefs.xml
```

## 🐛 Debugging Tips

### View Logs
```bash
# Filter by tag
adb logcat -s PhoneViewModel
adb logcat -s OTPVerification
adb logcat -s DriverDetailsViewModel

# View all app logs
adb logcat | grep "com.example.logistics_driver_app"
```

### Common Issues & Solutions

**Issue**: OTP not visible
- **Solution**: Check Logcat with filter "PhoneViewModel"

**Issue**: Navigation not working
- **Solution**: Check navigation graph includes all actions

**Issue**: Database errors
- **Solution**: Uninstall app and reinstall (clears database)

**Issue**: Validation errors
- **Solution**: Check ValidationUtils for format requirements

## 🎨 UI Testing

### Test Different Languages
1. Go back to Language Selection
2. Select different language (e.g., Hindi)
3. UI should update with Hindi strings
4. Test entire flow in new language

### Test Input Validation

**Phone Number**:
- ✅ Valid: `9876543210` (10 digits)
- ❌ Invalid: `123` (too short)
- ❌ Invalid: `98765432101` (too long)
- ❌ Invalid: `abcd123456` (contains letters)

**OTP**:
- ✅ Valid: `1234` (4 digits)
- ❌ Invalid: `12` (too short)
- ❌ Invalid: `abcd` (not numbers)

**Email**:
- ✅ Valid: `user@example.com`
- ❌ Invalid: `user@` (incomplete)
- ❌ Invalid: `user.com` (missing @)

**Vehicle Number**:
- ✅ Valid: `MH01AB1234`
- ✅ Valid: `MH-01-AB-1234`
- ❌ Invalid: `ABC123` (wrong format)

**Pincode**:
- ✅ Valid: `400001` (6 digits)
- ❌ Invalid: `4000` (too short)

## 📊 Performance Testing

### Memory Usage
```bash
# Check memory
adb shell dumpsys meminfo com.example.logistics_driver_app

# Monitor memory in real-time
adb shell top | grep logistics
```

### Database Performance
- Insert driver: < 100ms
- Query driver: < 50ms
- Update driver: < 100ms

## 🔐 Security Testing

### Test Cases
1. **SQL Injection**: Try special characters in inputs
2. **XSS**: Try HTML/JavaScript in text fields
3. **Buffer Overflow**: Try very long strings

**Expected**: All inputs are sanitized and validated

## 📝 Code Quality Checks

### Run Lint
```bash
./gradlew lint
```

### Run Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## 🎯 Sample Test Data

```kotlin
// Phone Numbers
val phoneNumbers = listOf(
    "9876543210",  // Valid Mumbai
    "8765432109",  // Valid
    "7654321098"   // Valid
)

// OTP Codes
// Check Logcat after sending OTP

// Driver Data
val sampleDriver = Driver(
    phoneNumber = "9876543210",
    name = "Rajesh Kumar",
    email = "rajesh@example.com",
    vehicleNumber = "MH01AB1234",
    vehicleType = "Truck",
    licenseNumber = "MH1234567890",
    address = "123 Main Street, Andheri",
    city = "Mumbai",
    state = "Maharashtra",
    pincode = "400001"
)
```

## 🚀 Build Variants

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## 📱 Device Testing

### Recommended Devices
- **Emulator**: Pixel 5 (API 31+)
- **Physical**: Any Android 7.0+ device

### Screen Sizes to Test
- Phone (360x640dp)
- Tablet (600x960dp)
- Large Tablet (1240x2048dp)

## ⚡ Quick Commands

```bash
# Install debug build
./gradlew installDebug

# Clear app data
adb shell pm clear com.example.logistics_driver_app

# Restart app
adb shell am start -n com.example.logistics_driver_app/.MainActivity

# Take screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Record video
adb shell screenrecord /sdcard/demo.mp4
# Stop with Ctrl+C
adb pull /sdcard/demo.mp4
```

## 📞 Need Help?

- Check README.md for architecture details
- Check PROJECT_SUMMARY.md for implementation details
- Review code comments for specific functionality

---

**Happy Coding! 🚀**
