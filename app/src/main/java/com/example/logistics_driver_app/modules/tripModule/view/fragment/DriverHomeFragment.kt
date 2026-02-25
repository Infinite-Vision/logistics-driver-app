package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.data.service.DriverLocationService
import com.example.logistics_driver_app.databinding.FragmentDriverHomeBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.DriverHomeViewModel
import com.example.logistics_driver_app.modules.tripModule.viewModel.TripFlowViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * DriverHomeFragment - Main home screen where driver can go online/offline
 * This is the first screen after login/verification
 */
class DriverHomeFragment : BaseFragment<FragmentDriverHomeBinding>() {

    companion object {
        private const val TAG = "DriverHomeFragment"
    }

    private val viewModel: DriverHomeViewModel by viewModels()
    private val tripFlowViewModel: TripFlowViewModel by activityViewModels()
    private var isOnline = false
    private var sliderStartX = 0f
    private var sliderMaxX = 0f
    private var canGoOnline = true
    
    private var outerPulseAnimator: ObjectAnimator? = null
    private var innerPulseAnimator: ObjectAnimator? = null
    private var isShowingTripRequest = false
    
    // BroadcastReceivers for service events
    private val driverOnlineReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "[BROADCAST] ✓ DRIVER_ONLINE broadcast received")
            isOnline = true
            updateUIForOnlineState()
            
            // Show toast confirming online mode
            Toast.makeText(context, "Going online - Connecting to server...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val driverOfflineReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "[BROADCAST] Driver went OFFLINE")
            isOnline = false
            updateUIForOfflineState()
            
            // Show toast confirming offline mode
            Toast.makeText(context, "You are now offline", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val newOrderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i(TAG, "[BROADCAST] ========================================")
            Log.i(TAG, "[BROADCAST] ✓ NEW_ORDER broadcast received in Fragment")
            Log.i(TAG, "[BROADCAST] Thread: ${Thread.currentThread().name}")
            Log.i(TAG, "[BROADCAST] ========================================")
            
            val orderId = intent?.getLongExtra("order_id", 0) ?: 0
            val pickup = intent?.getStringExtra("pickup_address") ?: ""
            val drop = intent?.getStringExtra("drop_address") ?: ""
            val distanceKm = intent?.getDoubleExtra("distance_km", 0.0) ?: 0.0
            val estimatedFare = intent?.getIntExtra("estimated_fare", 0) ?: 0
            val helperRequired = intent?.getBooleanExtra("helper_required", false) ?: false
            val customerName = intent?.getStringExtra("customer_name") ?: "Customer"
            
            Log.d(TAG, "[ORDER] Order #$orderId from $customerName")
            Log.d(TAG, "[ORDER] Pickup: $pickup → Drop: $drop, Fare: ₹$estimatedFare")
            
            // Show trip request bottom sheet with real order data
            showTripRequestBottomSheet(
                orderId = orderId,
                pickup = pickup,
                drop = drop,
                distanceKm = distanceKm,
                estimatedFare = estimatedFare,
                helperRequired = helperRequired,
                customerName = customerName
            )
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDriverHomeBinding {
        return FragmentDriverHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i(TAG, "[LIFECYCLE] ========================================")
        Log.i(TAG, "[LIFECYCLE] onViewCreated CALLED")
        Log.i(TAG, "[LIFECYCLE] Fragment: ${this.javaClass.simpleName}")
        Log.i(TAG, "[LIFECYCLE] ========================================")

        setupViews()
        setupObservers()
        setupSlider()
        registerBroadcastReceivers()
        
        // Check if service is already running and restore state
        // Delay slightly to ensure views are properly laid out
        binding.sliderButton.post {
            isOnline = DriverLocationService.isDriverOnline(requireContext())
            Log.d(TAG, "[INIT] Checking driver state: isOnline = $isOnline")
            if (isOnline) {
                Log.i(TAG, "[INIT] Service already running - restoring ONLINE state")
                updateUIForOnlineState()
            } else {
                Log.d(TAG, "[INIT] Driver is OFFLINE - showing Go Online slider")
                // Ensure slider is at start position
                binding.sliderButton.x = 4f
            }
        }
        
        // Fetch home summary data
        viewModel.fetchHomeSummary()
    }
    
    /**
     * Register broadcast receivers for service events
     */
    private fun registerBroadcastReceivers() {
        Log.i(TAG, "[BROADCAST] ========================================")
        Log.i(TAG, "[BROADCAST] registerBroadcastReceivers CALLED")
        Log.i(TAG, "[BROADCAST] ========================================")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                driverOnlineReceiver,
                IntentFilter("com.example.logistics_driver_app.DRIVER_ONLINE"),
                Context.RECEIVER_NOT_EXPORTED
            )
            requireContext().registerReceiver(
                driverOfflineReceiver,
                IntentFilter("com.example.logistics_driver_app.DRIVER_OFFLINE"),
                Context.RECEIVER_NOT_EXPORTED
            )
            requireContext().registerReceiver(
                newOrderReceiver,
                IntentFilter("com.example.logistics_driver_app.NEW_ORDER"),
                Context.RECEIVER_NOT_EXPORTED
            )
            Log.d(TAG, "[BROADCAST] Registered receivers (TIRAMISU+) for NEW_ORDER, DRIVER_ONLINE, DRIVER_OFFLINE")
        } else {
            requireContext().registerReceiver(
                driverOnlineReceiver,
                IntentFilter("com.example.logistics_driver_app.DRIVER_ONLINE")
            )
            requireContext().registerReceiver(
                driverOfflineReceiver,
                IntentFilter("com.example.logistics_driver_app.DRIVER_OFFLINE")
            )
            requireContext().registerReceiver(
                newOrderReceiver,
                IntentFilter("com.example.logistics_driver_app.NEW_ORDER")
            )
            Log.d(TAG, "[BROADCAST] Registered receivers (legacy) for NEW_ORDER, DRIVER_ONLINE, DRIVER_OFFLINE")
        }
        Log.d(TAG, "[BROADCAST] ✓ All receivers registered successfully")
    }

    private fun setupViews() {
        binding.apply {
            btnProfile.setOnClickListener {
                // Navigate to profile/settings
            }

            btnMenu.setOnClickListener {
                findNavController().navigate(R.id.action_driverHome_to_menu)
            }
        }
    }

    override fun setupObservers() {
        // Observe home summary data
        viewModel.homeSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                updateUI(it)
            }
        }

        // Observe canGoOnline status
        viewModel.canGoOnline.observe(viewLifecycleOwner) { canGo ->
            canGoOnline = canGo
            updateSliderState(canGo)
        }

        // Observe loading state
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe status verification state
        viewModel.statusVerifying.observe(viewLifecycleOwner) { isVerifying ->
            Log.d(TAG, "[STATUS_VERIFY] Verifying status: $isVerifying")
            if (isVerifying) {
                showStatusLoader()
            } else {
                hideStatusLoader()
            }
        }

        // Observe verified driver status
        viewModel.verifiedDriverStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                Log.i(TAG, "[STATUS_VERIFY] Status verified from backend: $status")
                when (status) {
                    "ONLINE" -> {
                        isOnline = true
                        updateUIForOnlineState()
                        Toast.makeText(context, "You are now online", Toast.LENGTH_SHORT).show()
                    }
                    "OFFLINE" -> {
                        isOnline = false
                        updateUIForOfflineState()
                        Toast.makeText(context, "You are now offline", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateUI(summary: com.example.logistics_driver_app.data.model.DriverHomeSummaryResponse) {
        binding.apply {
            // Update Today's Summary - Always visible
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            tvEarnings.text = currencyFormat.format(summary.todaySummary.earnings)
            tvTrips.text = summary.todaySummary.trips.toString()

            // Update status text
            // NOTE: isOnline is driven by the local service state (SharedPreference flag
            // reset on every fresh app launch), NOT by the server's driverStatus.
            // This prevents the UI from showing ONLINE after a force-kill where the
            // server still has the driver registered as ONLINE.
            val locallyOnline = DriverLocationService.isDriverOnline(requireContext())
            when (summary.driverStatus) {
                "OFFLINE" -> {
                    tvStatus.text = "You are Offline"
                    tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                    isOnline = false
                }
                "ONLINE" -> {
                    if (locallyOnline) {
                        tvStatus.text = "You are Online"
                        tvStatus.setTextColor(resources.getColor(R.color.primary, null))
                        isOnline = true
                    } else {
                        tvStatus.text = "You are Offline"
                        tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                        isOnline = false
                    }
                }
                "ON_TRIP" -> {
                    tvStatus.text = "On Trip"
                    tvStatus.setTextColor(resources.getColor(R.color.primary, null))
                    isOnline = locallyOnline
                }
                "BLOCKED" -> {
                    tvStatus.text = "Blocked"
                    tvStatus.setTextColor(resources.getColor(R.color.error, null))
                    isOnline = false
                }
                else -> {
                    tvStatus.text = "You are Offline"
                    tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                    isOnline = false
                }
            }
            
            // Update slider appearance based on isOnline state
            updateSliderState(summary.canGoOnline)

            // Handle block message
            if (!summary.canGoOnline && summary.block != null) {
                tvBlockMessage.text = summary.block.message
                tvBlockMessage.visibility = View.VISIBLE
                
                // Handle click on block message to redirect
                tvBlockMessage.setOnClickListener {
                    navigateToRedirectTarget(summary.block.redirectTo)
                }
            } else {
                tvBlockMessage.visibility = View.GONE
            }
        }
    }

    private fun updateSliderState(canGo: Boolean) {
        binding.apply {
            if (canGo) {
                // Enable slider
                sliderContainer.alpha = 1.0f
                sliderButton.isEnabled = true
                
                if (!isOnline) {
                    // Offline state - green slider
                    tvSliderText.text = "Go online"
                    sliderContainer.setBackgroundResource(R.drawable.bg_slider_track)
                    sliderButton.setBackgroundResource(R.drawable.bg_slider_button)
                } else {
                    // Online state - red slider
                    tvSliderText.text = "Go offline"
                    sliderContainer.setBackgroundResource(R.drawable.bg_slider_track_red)
                    sliderButton.setBackgroundResource(R.drawable.bg_slider_button_red)
                }
            } else {
                // Disable slider
                sliderContainer.alpha = 0.5f
                sliderButton.isEnabled = false
                tvSliderText.text = "Cannot go online"
                sliderContainer.setBackgroundResource(R.drawable.bg_slider_track)
                sliderButton.setBackgroundResource(R.drawable.bg_slider_button)
            }
        }
    }

    private fun showStatusLoader() {
        binding.apply {
            statusLoader.visibility = View.VISIBLE
            tvSliderText.visibility = View.GONE
            sliderButton.isEnabled = false
            Log.d(TAG, "[STATUS_VERIFY] Showing status verification loader")
        }
    }

    private fun hideStatusLoader() {
        binding.apply {
            statusLoader.visibility = View.GONE
            tvSliderText.visibility = View.VISIBLE
            sliderButton.isEnabled = true
            Log.d(TAG, "[STATUS_VERIFY] Hiding status verification loader")
        }
    }

    private fun navigateToRedirectTarget(redirectTo: String) {
        when (redirectTo) {
            "WALLET" -> {
                Toast.makeText(context, "Redirecting to Wallet...", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to wallet screen
            }
            "DOCUMENTS" -> {
                Toast.makeText(context, "Redirecting to Documents...", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to documents screen
            }
        }
    }

    private fun setupSlider() {
        binding.sliderButton.post {
            sliderMaxX = binding.sliderContainer.width.toFloat() - binding.sliderButton.width.toFloat() - 8f
            
            binding.sliderButton.setOnTouchListener { view, event ->
                // Only block if trying to go online when not allowed
                if (!isOnline && !canGoOnline) {
                    // Show message if cannot go online
                    viewModel.homeSummary.value?.block?.let {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                    return@setOnTouchListener true
                }
                
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        sliderStartX = view.x
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX - view.width / 2
                        val containerX = binding.sliderContainer.x
                        val relativeX = newX - containerX
                        
                        if (relativeX >= 0 && relativeX <= sliderMaxX) {
                            view.x = relativeX
                            // Update background color based on slider position
                            updateSliderBackground(relativeX / sliderMaxX)
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Slider always starts at START position
                        // Always slide RIGHT to toggle state (online/offline)
                        if (view.x >= sliderMaxX * 0.8f) {
                            // Slider completed - toggle state
                            view.x = sliderMaxX
                            onSliderCompleted()
                        } else {
                            // Reset slider to start
                            animateSliderReset(view)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun updateSliderBackground(progress: Float) {
        // Choose colors based on online state
        val (lightColor, darkColor) = if (!isOnline) {
            // Green colors for going online
            Pair(
                resources.getColor(R.color.primary_light, null),
                resources.getColor(R.color.primary, null)
            )
        } else {
            // Red colors for going offline
            Pair(
                resources.getColor(R.color.error_light, null),
                resources.getColor(R.color.error, null)
            )
        }
        
        val red = interpolate(android.graphics.Color.red(lightColor), android.graphics.Color.red(darkColor), progress)
        val green = interpolate(android.graphics.Color.green(lightColor), android.graphics.Color.green(darkColor), progress)
        val blue = interpolate(android.graphics.Color.blue(lightColor), android.graphics.Color.blue(darkColor), progress)
        
        val interpolatedColor = android.graphics.Color.rgb(red, green, blue)
        
        // Create a drawable with rounded corners to maintain shape
        val drawable = android.graphics.drawable.GradientDrawable()
        drawable.setColor(interpolatedColor)
        drawable.cornerRadius = 16f * resources.displayMetrics.density // 16dp in pixels
        binding.sliderContainer.background = drawable
    }
    
    private fun interpolate(start: Int, end: Int, fraction: Float): Int {
        return (start + (end - start) * fraction).toInt()
    }

    private fun animateSliderReset(view: View) {
        ObjectAnimator.ofFloat(view, "x", view.x, 4f).apply {
            duration = 200
            addUpdateListener { animation ->
                val progress = 1f - animation.animatedFraction
                updateSliderBackground(progress)
            }
            start()
        }
    }
    
    private fun animateSliderResetToEnd(view: View) {
        ObjectAnimator.ofFloat(view, "x", view.x, sliderMaxX).apply {
            duration = 200
            addUpdateListener { animation ->
                val progress = animation.animatedFraction
                updateSliderBackground(progress)
            }
            start()
        }
    }

    private fun onSliderCompleted() {
        if (!isOnline && canGoOnline) {
            // Go online
            Log.d(TAG, "[SLIDER] Slide completed - going ONLINE")
            updateOnlineStatus()
        } else if (isOnline) {
            // Go offline
            Log.d(TAG, "[SLIDER] Slide completed - going OFFLINE")
            updateOfflineStatus()
        }
        
        // DON'T reset slider here - let the broadcast receiver handle UI updates
        // This prevents the slider from jumping back and forth
    }
    
    /**
     * Reset slider to start position
     */
    private fun resetSliderPosition() {
        binding.sliderButton.post {
            ObjectAnimator.ofFloat(binding.sliderButton, "x", binding.sliderButton.x, 4f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }
    
    private fun updateOnlineStatus() {
        Log.d(TAG, "[SERVICE] Starting DriverLocationService to go ONLINE")
        
        try {
            // Start foreground service
            DriverLocationService.startService(requireContext())
            
            // Show loader and verify backend status
            Log.d(TAG, "[STATUS_VERIFY] Starting status verification for ONLINE")
            viewModel.verifyDriverStatus("ONLINE", maxAttempts = 15, delayMs = 1000L)
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Failed to start service: ${e.message}", e)
            Toast.makeText(requireContext(), "Error starting service: ${e.message}", Toast.LENGTH_LONG).show()
            hideStatusLoader()
        }
    }
    
    /**
     * Update UI to reflect online state (called from broadcast receiver)
     */
    private fun updateUIForOnlineState() {
        Log.d(TAG, "[UI] Updating UI to ONLINE state")
        
        // Only update if binding is available
        if (_binding == null) {
            Log.w(TAG, "[UI] Binding is null, skipping UI update")
            return
        }
        
        binding.apply {
            tvStatus.text = "You are Online"
            tvStatus.setTextColor(resources.getColor(R.color.primary, null))
            
            // Update slider to red for going offline
            tvSliderText.text = "Go offline"
            sliderContainer.setBackgroundResource(R.drawable.bg_slider_track_red)
            sliderButton.setBackgroundResource(R.drawable.bg_slider_button_red)
            
            // Reset slider to START position (so user can slide left to go offline)
            sliderButton.post {
                Log.d(TAG, "[UI] Resetting slider to START position: 4f")
                ObjectAnimator.ofFloat(sliderButton, "x", sliderButton.x, 4f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
            
            // Show Searching animation in center
            searchingContainer.visibility = View.VISIBLE
            
            // Start pulse animations
            startPulseAnimations()
        }
        
        Log.i(TAG, "[STATUS] UI updated to ONLINE - Animations started")
    }
    
    private fun updateOfflineStatus() {
        Log.d(TAG, "[SERVICE] Stopping DriverLocationService to go OFFLINE")
        
        // Stop foreground service - it will handle cleanup in goOffline()
        DriverLocationService.stopService(requireContext())
        
        // Show loader and verify backend status
        Log.d(TAG, "[STATUS_VERIFY] Starting status verification for OFFLINE")
        viewModel.verifyDriverStatus("OFFLINE", maxAttempts = 10, delayMs = 1000L)
    }
    
    /**
     * Update UI to reflect offline state (called from broadcast receiver)
     */
    private fun updateUIForOfflineState() {
        Log.d(TAG, "[UI] Updating UI to OFFLINE state")
        
        // Only update if binding is available
        if (_binding == null) {
            Log.w(TAG, "[UI] Binding is null, skipping UI update")
            return
        }
        
        binding.apply {
            tvStatus.text = "You are Offline"
            tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
            
            // Update slider to green for going online
            tvSliderText.text = "Go online"
            sliderContainer.setBackgroundResource(R.drawable.bg_slider_track)
            sliderButton.setBackgroundResource(R.drawable.bg_slider_button)
            
            // Move slider to START position (offline state)
            sliderButton.post {
                Log.d(TAG, "[UI] Moving slider to START position: 4f")
                ObjectAnimator.ofFloat(sliderButton, "x", sliderButton.x, 4f).apply {
                    duration = 300
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
            
            // Hide Searching animation
            searchingContainer.visibility = View.GONE
            
            // Stop pulse animations
            stopPulseAnimations()
        }
        
        Log.i(TAG, "[STATUS] UI updated to OFFLINE - Animations stopped")
    }
    
    private fun startPulseAnimations() {
        binding.apply {
            // Outer circle pulse animation
            outerPulseAnimator = ObjectAnimator.ofFloat(outerCircle, "scaleX", 1f, 1.2f).apply {
                duration = 1500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            
            ObjectAnimator.ofFloat(outerCircle, "scaleY", 1f, 1.2f).apply {
                duration = 1500
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            
            // Inner circle pulse animation (slightly delayed)
            innerPulseAnimator = ObjectAnimator.ofFloat(innerCircle, "scaleX", 1f, 1.15f).apply {
                duration = 1500
                startDelay = 300
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            
            ObjectAnimator.ofFloat(innerCircle, "scaleY", 1f, 1.15f).apply {
                duration = 1500
                startDelay = 300
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }
    
    private fun stopPulseAnimations() {
        // Cancel animators
        outerPulseAnimator?.cancel()
        outerPulseAnimator = null
        innerPulseAnimator?.cancel()
        innerPulseAnimator = null
        
        // Reset scales only if binding is available (prevents crash in onDestroyView)
        try {
            if (_binding != null) {
                binding.outerCircle.scaleX = 1f
                binding.outerCircle.scaleY = 1f
                binding.innerCircle.scaleX = 1f
                binding.innerCircle.scaleY = 1f
            }
        } catch (e: Exception) {
            Log.w(TAG, "[ANIMATION] Could not reset pulse animation scales: ${e.message}")
        }
    }
    
    private fun showTripRequestBottomSheet(
        orderId: Long,
        pickup: String,
        drop: String,
        distanceKm: Double,
        estimatedFare: Int,
        helperRequired: Boolean,
        customerName: String = "Customer"
    ) {
        Log.i(TAG, "[BOTTOM_SHEET] ========================================")
        Log.i(TAG, "[BOTTOM_SHEET] showTripRequestBottomSheet CALLED")
        Log.i(TAG, "[BOTTOM_SHEET] Order #$orderId from $customerName")
        Log.i(TAG, "[BOTTOM_SHEET] Thread: ${Thread.currentThread().name}")
        Log.i(TAG, "[BOTTOM_SHEET] ========================================")
        
        // Prevent showing multiple bottom sheets
        if (isShowingTripRequest) {
            Log.w(TAG, "[ORDER] Bottom sheet already showing, skipping")
            return
        }
        
        isShowingTripRequest = true
        Log.d(TAG, "[BOTTOM_SHEET] Set isShowingTripRequest = true")
        
        // Format distance display
        val tripDistance = if (distanceKm > 0) "~${String.format("%.1f", distanceKm)} km trip" else "Distance not available"
        
        val bottomSheet = TripRequestBottomSheet.newInstance(
            pickupAddress = pickup,
            pickupDistance = "Nearby",  // We don't have driver's current location to calculate this
            dropAddress = drop,
            tripDistance = tripDistance,
            packageType = "Package",  // Default value, server doesn't send this yet
            packageWeight = "Unknown",  // Default value, server doesn't send this yet
            estimatedFare = estimatedFare,
            helperRequired = helperRequired
        )
        
        bottomSheet.setOnAcceptListener {
            isShowingTripRequest = false
            Log.i(TAG, "[ORDER] ========================================")
            Log.i(TAG, "[ORDER] Driver accepted order #$orderId")
            Log.i(TAG, "[ORDER] ========================================")
            
            // Call API to accept order
            viewModel.acceptOrder(
                orderId = orderId,
                onSuccess = {
                    Log.i(TAG, "[ORDER] ✓ Order #$orderId successfully accepted via API")
                    // Reset all stale LiveData from the previous trip so fragments
                    // don't skip past their screens when they re-subscribe.
                    tripFlowViewModel.resetForNewTrip()
                    tripFlowViewModel.setOrderId(orderId)
                    // Navigate to pickup arrival screen
                    try {
                        findNavController().navigate(R.id.action_driverHome_to_pickupArrival)
                        Log.d(TAG, "[NAVIGATION] Navigated to pickup arrival screen")
                    } catch (e: Exception) {
                        Log.e(TAG, "[NAVIGATION] Error navigating: ${e.message}", e)
                        Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { errorMessage ->
                    Log.e(TAG, "[ORDER] ✗ Failed to accept order #$orderId: $errorMessage")
                    Toast.makeText(context, "Failed to accept order: $errorMessage", Toast.LENGTH_LONG).show()
                }
            )
        }
        
        bottomSheet.setOnDeclineListener {
            isShowingTripRequest = false
            Log.i(TAG, "[ORDER] ========================================")
            Log.i(TAG, "[ORDER] Driver declined order #$orderId")
            Log.i(TAG, "[ORDER] ========================================")
            
            // Call API to reject order
            viewModel.rejectOrder(
                orderId = orderId,
                onSuccess = {
                    Log.i(TAG, "[ORDER] ✓ Order #$orderId successfully rejected via API")
                    Toast.makeText(context, "Order declined", Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Log.e(TAG, "[ORDER] ✗ Failed to reject order #$orderId: $errorMessage")
                    Toast.makeText(context, "Failed to decline order: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        bottomSheet.setOnTimeoutListener {
            isShowingTripRequest = false
            Log.w(TAG, "[ORDER] Order $orderId timeout (30s expired)")
            Toast.makeText(context, "Request timeout", Toast.LENGTH_SHORT).show()
            
            // TODO: Timeout should auto-reject on server side
        }
        
        // Ensure flag is reset even if dialog is dismissed unexpectedly
        bottomSheet.setOnDismissListener {
            isShowingTripRequest = false
        }
        
        try {
            Log.d(TAG, "[BOTTOM_SHEET] Attempting to show bottom sheet...")
            bottomSheet.show(childFragmentManager, "TripRequestBottomSheet")
            Log.d(TAG, "[BOTTOM_SHEET] ✓ Bottom sheet shown for order #$orderId from $customerName")
        } catch (e: Exception) {
            Log.e(TAG, "[BOTTOM_SHEET] ERROR showing bottom sheet: ${e.message}", e)
            isShowingTripRequest = false
        }
    }
    
    override fun onDestroyView() {
        Log.d(TAG, "[LIFECYCLE] onDestroyView - Cleaning up resources")
        
        // Unregister broadcast receivers
        try {
            requireContext().unregisterReceiver(driverOnlineReceiver)
            requireContext().unregisterReceiver(driverOfflineReceiver)
            requireContext().unregisterReceiver(newOrderReceiver)
            Log.d(TAG, "[BROADCAST] Receivers unregistered")
        } catch (e: Exception) {
            Log.w(TAG, "[BROADCAST] Error unregistering receivers: ${e.message}")
        }
        
        // Stop animations and timers before view is destroyed
        stopPulseAnimations()
        isShowingTripRequest = false
        
        // NOTE: Service continues running in background even when view is destroyed
        // User must explicitly go offline to stop the service
        
        // Call super AFTER cleanup to ensure binding is still available
        super.onDestroyView()
        
        Log.d(TAG, "[LIFECYCLE] onDestroyView completed - Service continues in background")
    }
}
