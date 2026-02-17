package com.example.logistics_driver_app.data.model

import com.google.gson.annotations.SerializedName

// ============= Common Response Wrappers =============

/**
 * Standard API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("errorCode")
    val errorCode: String?
)

/**
 * Simple message response
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String
)

// ============= Authentication Models =============

/**
 * Logout response
 */
data class LogoutResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

// ============= App State Models =============

/**
 * App state request (no body needed)
 */
data class AppStateResponse(
    @SerializedName("onboardingStatus")
    val onboardingStatus: String? = null,
    @SerializedName("driverStatus")
    val driverStatus: String? = null,
    @SerializedName("nextScreen")
    val nextScreen: String? = null,
    @SerializedName("preferredLanguage")
    val preferredLanguage: String? = null
)

// ============= User Preferences Models =============

/**
 * Update language request
 */
data class UpdateLanguageRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("preferredLanguage")
    val preferredLanguage: String
)

/**
 * Update language response
 */
data class UpdateLanguageResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("preferredLanguage")
    val preferredLanguage: String
)

// ============= Onboarding Owner Models =============

/**
 * Save owner request
 */
data class SaveOwnerRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("ownerSelfieUrl")
    val ownerSelfieUrl: String,
    @SerializedName("ownerAdharUrl")
    val ownerAdharUrl: String,
    @SerializedName("ownerPanUrl")
    val ownerPanUrl: String
)

/**
 * Owner response
 */
data class OwnerResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("ownerSelfieDocumentId")
    val ownerSelfieDocumentId: Int,
    @SerializedName("ownerAdharDocumentId")
    val ownerAdharDocumentId: Int,
    @SerializedName("ownerPanDocumentId")
    val ownerPanDocumentId: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

// ============= Onboarding Vehicle Models =============

/**
 * Save vehicle request
 */
data class SaveVehicleRequest(
    @SerializedName("registrationNumber")
    val registrationNumber: String,
    @SerializedName("vehicleType")
    val vehicleType: String,
    @SerializedName("bodyType")
    val bodyType: String,
    @SerializedName("bodySpec")
    val bodySpec: String,
    @SerializedName("rcUrl")
    val rcUrl: String,
    @SerializedName("insuranceUrl")
    val insuranceUrl: String,
    @SerializedName("pucUrl")
    val pucUrl: String
)

/**
 * Vehicle response
 */
data class VehicleResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("registrationNumber")
    val registrationNumber: String,
    @SerializedName("city")
    val city: String,
    @SerializedName("vehicleType")
    val vehicleType: String,
    @SerializedName("bodyType")
    val bodyType: String,
    @SerializedName("bodySpec")
    val bodySpec: String,
    @SerializedName("rcDocumentId")
    val rcDocumentId: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)

// ============= Onboarding Driver Models =============

/**
 * Save driver request
 */
data class SaveDriverRequest(
    @SerializedName("isSelfDriving")
    val isSelfDriving: Boolean,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("driverLicenseUrl")
    val driverLicenseUrl: String?
)

/**
 * Driver response
 */
data class DriverResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("isSelfDriving")
    val isSelfDriving: Boolean?,
    @SerializedName("driverLicenseDocumentId")
    val driverLicenseDocumentId: Int?,
    @SerializedName("onboardingStep")
    val onboardingStep: String?,
    @SerializedName("verificationStatus")
    val verificationStatus: String?
)

// ============= Meta Models =============

/**
 * Form option item
 */
data class FormOption(
    @SerializedName("id")
    val id: Int,
    @SerializedName("code")
    val code: String,
    @SerializedName("displayName")
    val displayName: String
)

/**
 * Vehicle form options response
 */
data class VehicleFormOptionsResponse(
    @SerializedName("vehicleTypes")
    val vehicleTypes: List<FormOption>,
    @SerializedName("bodyTypes")
    val bodyTypes: List<FormOption>,
    @SerializedName("bodySpecs")
    val bodySpecs: List<FormOption>
)

// ============= Enums =============

/**
 * Language codes
 */
enum class LanguageCode(val code: String) {
    ENGLISH("EN"),
    TAMIL("TA"),
    HINDI("HI")
}

/**
 * Vehicle types
 */
enum class VehicleType(val code: String) {
    TRUCK("TRUCK"),
    MINI_TRUCK("MINI_TRUCK"),
    THREE_WHEELER("THREE_WHEELER"),
    PICKUP("PICKUP")
}

/**
 * Body types
 */
enum class BodyType(val code: String) {
    OPEN("OPEN"),
    CLOSED("CLOSED"),
    SEMI_OPEN("SEMI_OPEN")
}

/**
 * Body specifications
 */
enum class BodySpec(val code: String) {
    EIGHT_FT_1_5_TON("EIGHT_FT_1_5_TON"),
    FOURTEEN_FT_3_5_TON("FOURTEEN_FT_3_5_TON"),
    SEVENTEEN_FT_4_5_TON("SEVENTEEN_FT_4_5_TON"),
    NINETEEN_FT_6_TON("NINETEEN_FT_6_TON")
}

/**
 * Onboarding status
 */
enum class OnboardingStatus(val status: String) {
    NOT_STARTED("NOT_STARTED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    SUBMITTED("SUBMITTED")
}

/**
 * Onboarding step
 */
enum class OnboardingStep(val step: String) {
    OWNER("OWNER"),
    VEHICLE("VEHICLE"),
    DRIVER("DRIVER"),
    SUBMITTED("SUBMITTED")
}

/**
 * Driver status
 */
enum class DriverStatus(val status: String) {
    ONLINE("ONLINE"),
    OFFLINE("OFFLINE"),
    BUSY("BUSY")
}

/**
 * Verification status
 */
enum class VerificationStatus(val status: String) {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    VERIFIED("VERIFIED"),
    REJECTED("REJECTED")
}

// ============= Driver Home Module =============

/**
 * Driver Home Summary response
 */
data class DriverHomeSummaryResponse(
    @SerializedName("driverStatus")
    val driverStatus: String,
    @SerializedName("canGoOnline")
    val canGoOnline: Boolean,
    @SerializedName("block")
    val block: BlockInfo?,
    @SerializedName("todaySummary")
    val todaySummary: TodaySummary
)

/**
 * Block information when driver cannot go online
 */
data class BlockInfo(
    @SerializedName("reason")
    val reason: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("redirectTo")
    val redirectTo: String
)

/**
 * Today's summary metrics
 */
data class TodaySummary(
    @SerializedName("earnings")
    val earnings: Double,
    @SerializedName("trips")
    val trips: Int,
    @SerializedName("hoursOnline")
    val hoursOnline: Double,
    @SerializedName("distanceKm")
    val distanceKm: Double
)

/**
 * Driver operational status enum
 */
enum class DriverOperationalStatus(val status: String) {
    OFFLINE("OFFLINE"),
    ONLINE("ONLINE"),
    ON_TRIP("ON_TRIP"),
    BLOCKED("BLOCKED")
}

/**
 * Block reason enum
 */
enum class BlockReason(val reason: String) {
    LOW_BALANCE("LOW_BALANCE"),
    DOCUMENT_EXPIRED("DOCUMENT_EXPIRED"),
    ADMIN_BLOCKED("ADMIN_BLOCKED")
}

/**
 * Redirect target enum
 */
enum class RedirectTarget(val target: String) {
    WALLET("WALLET"),
    DOCUMENTS("DOCUMENTS")
}
