# Logistics Driver App

A comprehensive Android application for logistics driver onboarding and management, built with modern Android development practices.

## 📱 Features

### Authentication Module
- **Language Selection**: Multi-language support (English, Hindi, Tamil, Telugu, Kannada, Marathi)
- **Phone Number Entry**: Validates Indian mobile numbers (+91)
- **OTP Verification**: 4-digit OTP verification with resend timer (30 seconds)
- **Session Management**: Secure authentication using SharedPreferences and Room DB

### Driver Details Module
- **Personal Information**: Name, Email, Address, City, State, Pincode
- **Vehicle Information**: Vehicle Number, Vehicle Type, License Number
- **CRUD Operations**: Full create, read, update, delete functionality on local database
- **Input Validation**: Comprehensive validation for all fields

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern:

```
app/
├── data/
│   ├── local/
│   │   ├── dao/            # Room Database DAOs
│   │   ├── database/       # Room Database instance
│   │   └── preferences/    # SharedPreferences Manager
│   ├── model/              # Data models (Driver, AuthSession, Language)
│   ├── remote/             # Retrofit API Service (prepared for future)
│   └── repository/         # Repository pattern implementations
├── ui/
│   ├── auth/
│   │   ├── language/       # Language Selection Fragment & ViewModel
│   │   ├── phone/          # Phone Entry Fragment & ViewModel
│   │   └── otp/            # OTP Verification Fragment & ViewModel
│   └── driver/             # Driver Details Fragment & ViewModel
├── utils/                  # Utility classes
│   ├── CommonUtils.kt      # Common helper functions
│   ├── ValidationUtils.kt  # Input validation utilities
│   ├── Constants.kt        # App-wide constants
│   └── LanguageUtils.kt    # Language management utilities
└── MainActivity.kt         # Main app entry point
```

## 🛠️ Tech Stack

### Core Technologies
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Libraries & Dependencies
- **Architecture Components**:
  - ViewBinding
  - LiveData
  - ViewModel
  - Navigation Component
  
- **Database**:
  - Room Database (2.6.1)
  - Kotlin Coroutines for async operations
  
- **Networking** (Prepared for future):
  - Retrofit (2.9.0)
  - OkHttp (4.12.0)
  - Gson Converter
  
- **UI**:
  - Material Design Components (1.13.0)
  - ConstraintLayout
  - RecyclerView

## 📂 Key Components

### Data Layer

#### Models
- **Driver**: Entity for driver information with Room annotations
- **AuthSession**: Entity for authentication session tracking
- **Language**: Data class for language selection

#### DAOs (Data Access Objects)
- **DriverDao**: CRUD operations for driver data
- **AuthDao**: Authentication session management

#### Repositories
- **DriverRepository**: Abstraction layer for driver data operations
- **AuthRepository**: Abstraction layer for auth operations

### Presentation Layer

#### Language Selection
- Grid layout with flag emojis
- Material card design
- Selected state highlighting

#### Phone Entry
- Country code (+91) display
- 10-digit validation
- Material text input design

#### OTP Verification
- 4-digit input fields
- Auto-focus between fields
- Countdown timer for resend
- Backspace handling

#### Driver Details
- Scrollable form layout
- Section-wise organization (Personal & Vehicle info)
- Real-time validation
- Error display on fields

### Utils

#### ValidationUtils
- Phone number validation (10 digits)
- OTP validation (4 digits)
- Email validation
- Vehicle number validation (Indian format)
- License number validation
- Pincode validation (6 digits)
- Name validation

#### CommonUtils
- Toast/Snackbar display
- Keyboard show/hide
- Date/Time formatting
- OTP generation (for testing)
- String manipulation

#### PreferencesManager
- Language preference
- Phone number storage
- Login state
- Session token
- Onboarding completion status

## 🌍 Localization

The app supports multiple languages with separate string resource files:

- **English** (`values/strings.xml`)
- **Hindi** (`values-hi/strings.xml`)
- More languages can be added following the same pattern

### Adding New Language

1. Create new folder: `values-{language-code}/`
2. Add `strings.xml` with translated strings
3. Update `LanguageUtils.kt` with language data
4. Update Language Selection screen

## 🎨 UI Design

### Color Scheme
- **Primary**: #5FD068 (Green)
- **Background**: #FFFFFF (White)
- **Text Primary**: #000000 (Black)
- **Text Secondary**: #757575 (Gray)

### Design Principles
- Material Design 3
- Rounded corners (12dp)
- Consistent padding (24dp)
- Clear visual hierarchy
- Accessible touch targets

## 🔧 Setup & Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   ```

2. **Open in Android Studio**
   - Android Studio Hedgehog or later recommended
   - Gradle will sync automatically

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   - Select device
   - Click Run button

## 🧪 Testing

### Frontend Testing (No API)

Since there's no backend API currently:

1. **OTP Generation**: OTP is generated locally and logged in console
2. **Data Storage**: All data stored in local Room database
3. **Session Management**: Using SharedPreferences

**To test OTP verification:**
- Enter any valid phone number
- Check Logcat for generated OTP
- Enter the OTP shown in logs

## 📝 Code Documentation

All classes and methods include comprehensive documentation:

```kotlin
/**
 * Brief description of the class/method purpose.
 * @param paramName Parameter description
 * @return Return value description
 */
```

## 🔐 Security Considerations

- Phone numbers validated before storage
- Session tokens generated for auth tracking
- No hardcoded credentials
- Input sanitization in all forms

## 🚀 Future Enhancements

### Backend Integration
- Connect Retrofit to actual API
- Implement real OTP service
- Server-side validation
- User authentication tokens

### Additional Features
- Profile image upload
- Document verification (License, RC)
- Order management
- Delivery tracking
- Earnings dashboard
- Notification system

## 📱 Screens Flow

```
1. Language Selection
   ↓
2. Phone Number Entry
   ↓
3. OTP Verification
   ↓
4. Driver Details
   ↓
5. Home Dashboard (Future)
```

## 🐛 Known Issues

- None currently. App is in development phase.

## 📄 License

[Add your license here]

## 👥 Contributors

[Add contributors here]

## 📞 Support

For issues or questions, please open an issue on GitHub.

---

**Built with ❤️ using Kotlin and Android Jetpack**
