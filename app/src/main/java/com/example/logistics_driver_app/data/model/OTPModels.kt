package com.example.logistics_driver_app.data.model

import com.google.gson.annotations.SerializedName

/**
 * OTP Request model for sending OTP to phone number
 */
data class OTPRequest(
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String
)

/**
 * OTP Response model
 */
data class OTPResponse(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("data")
    val data: OTPData?
)

data class OTPData(
    @SerializedName("message")
    val message: String?
)

/**
 * OTP Verify Request model
 */
data class OTPVerifyRequest(
    @SerializedName("countryCode")
    val countryCode: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("preferredLanguageCode")
    val preferredLanguageCode: String
)

/**
 * OTP Verify Response model
 */
data class OTPVerifyResponse(
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("data")
    val data: VerifyData?
)

data class VerifyData(
    @SerializedName("message")
    val message: String?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("onboardingStatus")
    val onboardingStatus: String?,
    @SerializedName("onboardingStep")
    val onboardingStep: String?
)
