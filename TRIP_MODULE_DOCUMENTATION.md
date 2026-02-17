# Trip Module Documentation

## Overview
The Trip Module handles all trip-related functionality for drivers, including trip acceptance, navigation, pickup/drop operations, payment collection, and trip completion.

## Design Reference
- **t1.png to t18.png**: Trip start and progress screens
- **t8.png and t9.png**: Arrival screens with button at bottom (not slider) to mark arrived
- **t15.png**: Trip info sheet that appears when clicking top left red icon
- **t16-t18.png**: Trip completion screens
- **t19.png to t22.png**: Menu screens
- **tp1.png and tp2.png**: Payment collection screens
- **tc1.png and tc2.png**: Trip cancellation screens (top right cancel button is visible)

## Module Structure

### Data Models (`data/model/TripModels.kt`)
- **Trip**: Main trip data model
  - Trip information (pickup/drop addresses, contact details)
  - Payment details (amount, mode, status)
  - Trip status tracking
  - Timestamps for various stages
  
- **TripStatus**: Enum for trip states
  - TRIP_ASSIGNED
  - HEADING_TO_PICKUP
  - ARRIVED_AT_PICKUP
  - STARTED_TRIP
  - HEADING_TO_DROP
  - ARRIVED_AT_DROP
  - TRIP_COMPLETED
  - TRIP_CANCELLED

- **PaymentStatus**: Enum for payment states
  - PENDING
  - CASH_COLLECTED
  - ONLINE_PAID
  - FAILED

- **CancellationReasons**: Pre-defined cancellation reasons
  - Customer not available
  - Wrong address
  - Vehicle breakdown
  - Customer cancelled
  - Traffic issue
  - Other

### ViewModels (`modules/tripModule/viewModel/`)

#### BaseTripViewModel
Base class for all trip-related ViewModels providing:
- Current trip data management
- Trip status updates
- Common trip operations

#### TripActiveViewModel
- Manages active trip display
- Navigation start/stop
- Estimated arrival updates

#### TripPickupViewModel
- Pickup OTP verification
- Pickup arrival confirmation

#### TripDropViewModel
- Drop OTP verification
- Payment collection handling
- Trip completion

#### PaymentViewModel
- Cash payment confirmation
- Online payment verification
- Payment status tracking

#### CancellationViewModel
- Cancellation reason selection
- Cancellation submission
- Cancellation confirmation

#### TripMenuViewModel
- Menu navigation
- Driver statistics display
- Menu action handling

### Fragments and Layouts

#### 1. TripActiveFragment (t1-t7)
**Layout**: `fragment_trip_active.xml`
**Features**:
- Map view placeholder for route display
- Top bar with menu icon (red) and trip status badge
- Bottom sheet with:
  - Order ID and estimated arrival time
  - Pickup and drop location details with call buttons
  - Payment information card
  - Start Navigation button
  - Cancel Trip button

**Usage**:
```kotlin
// Navigate to this fragment from your main activity
findNavController().navigate(R.id.tripActiveFragment)
```

#### 2. PickupArrivalFragment (t8-t9)
**Layout**: `fragment_pickup_arrival.xml`
**Features**:
- Map view at top
- Details card with:
  - Pickup location
  - Customer contact info with call button
  - OTP input field
  - Item description
- **Button at bottom** to mark arrived (not a slider as per requirement)

**Key Implementation**:
```kotlin
// OTP verification before marking arrived
val otp = etOtp.text.toString().trim()
if (otp.length == 4 && viewModel.verifyPickupOTP(otp)) {
    viewModel.updateTripStatus(TripStatus.STARTED_TRIP)
}
```

#### 3. DropArrivalFragment (t8-t9 for drop)
**Layout**: `fragment_drop_arrival.xml`
**Features**:
- Similar to pickup but for drop location
- Drop OTP verification
- Payment summary
- **Button at bottom** to complete trip

**Key Implementation**:
```kotlin
// Verify OTP and collect payment
if (isValid && trip.paymentMode == "CASH") {
    viewModel.collectPayment(trip.amount)
}
viewModel.updateTripStatus(TripStatus.TRIP_COMPLETED)
```

#### 4. TripInfoSheetFragment (t15)
**Layout**: `fragment_trip_info_sheet.xml`
**Features**:
- Shows when clicking **top left red icon**
- Comprehensive trip details:
  - Order ID and trip status
  - Complete pickup details
  - Complete drop details
  - Item description and payment info

**Trigger**:
```kotlin
// In TripActiveFragment
btnMenu.setOnClickListener {
    findNavController().navigate(R.id.action_tripActive_to_tripInfoSheet)
}
```

#### 5. TripCompletedFragment (t16-t18)
**Layout**: `fragment_trip_completed.xml`
**Features**:
- Success icon and congratulations message
- Trip summary card showing:
  - Order ID
  - Distance traveled
  - Payment mode
  - Amount earned
- View Earnings button
- Continue button

#### 6. PaymentCollectionFragment (tp1-tp2)
**Layout**: `fragment_payment_collection.xml`
**Features**:
- Large amount display
- Payment mode badge (CASH/ONLINE)
- Trip details summary
- Payment instructions
- Confirm Payment button

**Usage**:
```kotlin
// Confirm cash payment
viewModel.confirmCashPayment(trip.amount)

// Confirm online payment
viewModel.confirmOnlinePayment(transactionId)
```

#### 7. CancelTripFragment (tc1-tc2)
**Layout**: `fragment_cancel_trip.xml`
**Features**:
- **Top right cancel button is visible** (as per requirement)
- Warning message
- Trip information summary
- Cancellation reason dropdown/spinner
- Additional notes text field
- Submit Cancellation button

**Key Implementation**:
```kotlin
// Cancel button in top right
btnCancel.setOnClickListener {
    findNavController().navigateUp()
}

// Submit cancellation
val reason = spinnerCancelReason.selectedItem.toString()
val notes = etAdditionalNotes.text.toString()
viewModel.submitCancellation(reason, notes)
```

#### 8. MenuFragment (t19-t22)
**Layout**: `fragment_menu.xml`
**Features**:
- Driver profile card
- Today's earnings card with statistics
- Menu options:
  - View Earnings
  - Trip History
  - Support
  - Settings
- Logout button

### Common Components

#### Drawables
- `bg_button_primary.xml`: Primary action buttons (green)
- `bg_button_secondary.xml`: Secondary buttons (white with green border)
- `bg_button_danger.xml`: Danger/cancel buttons (red)
- `bg_card.xml`: Card backgrounds
- `bg_bottom_sheet.xml`: Bottom sheet rounded top corners
- `bg_icon_circle.xml`: Circular icon backgrounds
- `bg_status_badge.xml`: Status badge backgrounds

#### Icons
- `ic_location.xml`: Location pin icon
- `ic_navigation.xml`: Navigation arrow icon
- `ic_call.xml`: Phone call icon
- `ic_menu.xml`: Menu/hamburger icon
- `ic_close.xml`: Close/cancel icon
- `ic_check.xml`: Checkmark icon
- `ic_info.xml`: Information icon
- `ic_money.xml`: Rupee/money icon

### Navigation Graph (`navigation/trip_navigation.xml`)

**Start Destination**: `tripActiveFragment`

**Navigation Flow**:
```
tripActiveFragment
├── pickupArrivalFragment (mark arrived at pickup)
├── dropArrivalFragment (mark arrived at drop)
│   ├── paymentCollectionFragment (optional)
│   └── tripCompletedFragment
├── tripInfoSheetFragment (click red icon)
├── cancelTripFragment
└── menuFragment
```

### String Resources

All strings are defined in `values/strings.xml` under the "Trip Module Strings" section, including:
- Trip status labels
- Action button labels
- Trip information labels
- Payment labels
- Cancellation reasons
- Menu items
- Success/error messages
- Formatted strings (amounts, distances, times)

### Key Requirements Implemented

✅ **t8.png and t9.png**: Button at bottom (not slider) to mark arrived at pickup/drop
✅ **t15.png**: Trip info sheet appears when clicking top left red icon
✅ **tc1.png and tc2.png**: Top right cancel button is visible in cancellation screen
✅ **No APIs**: All ViewModels use mock data and local state management
✅ **Common Components**: Reusable drawables, icons, and string resources

### Usage Example

#### Starting a Trip Flow
```kotlin
// 1. Driver sees active trip
TripActiveFragment

// 2. Driver clicks "Start Navigation"
startGoogleMapsNavigation()

// 3. Driver arrives at pickup, clicks menu icon
// Shows TripInfoSheetFragment with full details

// 4. Driver arrives at pickup location
navigate to PickupArrivalFragment

// 5. Enter OTP and click "Mark Arrived" button
verifyPickupOTP() → updateTripStatus(STARTED_TRIP)

// 6. Drive to drop location

// 7. Arrive at drop location
navigate to DropArrivalFragment

// 8. Verify OTP, collect payment, click "Complete Trip" button
verifyDropOTP() → collectPayment() → updateTripStatus(COMPLETED)

// 9. Show completion screen
navigate to TripCompletedFragment
```

#### Cancelling a Trip
```kotlin
// From TripActiveFragment
btnCancelTrip.setOnClickListener {
    navigate to CancelTripFragment
}

// Select reason from dropdown
// Add optional notes
// Click "Submit Cancellation"
viewModel.submitCancellation(reason, notes)
```

### Testing

Since no APIs are implemented yet, all data is mocked in ViewModels:
- Mock trip data in `BaseTripViewModel.loadCurrentTrip()`
- OTP verification always succeeds for 4-digit codes
- Payment confirmation is instant
- Cancellation submission is immediate

### Future Enhancements

When APIs become available:
1. Replace mock data loading with actual API calls
2. Implement real-time location tracking
3. Add push notifications for trip updates
4. Integrate actual Google Maps SDK
5. Implement real OTP verification
6. Add payment gateway integration
7. Implement trip history and earnings screens

### Integration with Main App

Add trip navigation to your main navigation graph:
```xml
<include app:graph="@navigation/trip_navigation" />
```

Or navigate programmatically:
```kotlin
findNavController().navigate(R.id.trip_navigation)
```

## Design Compliance

✅ All screens match the provided design images (t1-t22, tp1-tp2, tc1-tc2)
✅ Buttons instead of sliders for arrival confirmation
✅ Top left red icon opens trip info sheet
✅ Top right cancel button visible in cancellation screen
✅ Common components used throughout for consistency
✅ No API dependencies - ready for future integration

---

**Module Status**: ✅ Complete and ready for testing
**Last Updated**: February 13, 2026
