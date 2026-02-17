package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.FragmentDriverHomeBinding
import com.example.logistics_driver_app.modules.loginModule.base.BaseFragment
import com.example.logistics_driver_app.modules.tripModule.viewModel.DriverHomeViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * DriverHomeFragment - Main home screen where driver can go online/offline
 * This is the first screen after login/verification
 */
class DriverHomeFragment : BaseFragment<FragmentDriverHomeBinding>() {

    private val viewModel: DriverHomeViewModel by viewModels()
    private var isOnline = false
    private var sliderStartX = 0f
    private var sliderMaxX = 0f
    private var canGoOnline = true
    
    private var outerPulseAnimator: ObjectAnimator? = null
    private var innerPulseAnimator: ObjectAnimator? = null
    private val tripRequestHandler = Handler(Looper.getMainLooper())
    private var tripRequestRunnable: Runnable? = null
    private var isShowingTripRequest = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDriverHomeBinding {
        return FragmentDriverHomeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupObservers()
        setupSlider()
        
        // Fetch home summary data
        viewModel.fetchHomeSummary()
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
    }

    private fun updateUI(summary: com.example.logistics_driver_app.data.model.DriverHomeSummaryResponse) {
        binding.apply {
            // Update Today's Summary - Always visible
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            tvEarnings.text = currencyFormat.format(summary.todaySummary.earnings)
            tvTrips.text = summary.todaySummary.trips.toString()

            // Update status text
            when (summary.driverStatus) {
                "OFFLINE" -> {
                    tvStatus.text = "You are Offline"
                    tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
                    isOnline = false
                }
                "ONLINE" -> {
                    tvStatus.text = "You are Online"
                    tvStatus.setTextColor(resources.getColor(R.color.primary, null))
                    isOnline = true
                }
                "ON_TRIP" -> {
                    tvStatus.text = "On Trip"
                    tvStatus.setTextColor(resources.getColor(R.color.primary, null))
                    isOnline = true
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
                if (!canGoOnline) {
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
                        if (view.x >= sliderMaxX * 0.8f) {
                            // Slider completed
                            view.x = sliderMaxX
                            onSliderCompleted()
                        } else {
                            // Reset slider
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

    private fun onSliderCompleted() {
        if (!isOnline && canGoOnline) {
            // Go online
            isOnline = true
            updateOnlineStatus()
        } else if (isOnline) {
            // Go offline
            isOnline = false
            updateOfflineStatus()
        }
    }
    
    private fun updateOnlineStatus() {
        binding.apply {
            tvStatus.text = "You are Online"
            tvStatus.setTextColor(resources.getColor(R.color.primary, null))
            
            // Update slider to red for going offline
            tvSliderText.text = "Go offline"
            sliderContainer.setBackgroundResource(R.drawable.bg_slider_track_red)
            sliderButton.setBackgroundResource(R.drawable.bg_slider_button_red)
            
            // Reset slider position
            sliderButton.x = 4f
            
            // Show Searching animation in center
            searchingContainer.visibility = View.VISIBLE
            
            // Start pulse animations
            startPulseAnimations()
            
            // Start showing trip requests every 5 seconds
            startTripRequestTimer()
        }
    }
    
    private fun updateOfflineStatus() {
        binding.apply {
            tvStatus.text = "You are Offline"
            tvStatus.setTextColor(resources.getColor(R.color.text_secondary, null))
            
            // Update slider to green for going online
            tvSliderText.text = "Go online"
            sliderContainer.setBackgroundResource(R.drawable.bg_slider_track)
            sliderButton.setBackgroundResource(R.drawable.bg_slider_button)
            
            // Reset slider position
            sliderButton.x = 4f
            
            // Hide Searching animation
            searchingContainer.visibility = View.GONE
            
            // Stop pulse animations
            stopPulseAnimations()
            
            // Stop trip request timer
            stopTripRequestTimer()
        }
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
        outerPulseAnimator?.cancel()
        outerPulseAnimator = null
        innerPulseAnimator?.cancel()
        innerPulseAnimator = null
        
        // Reset scales
        binding.outerCircle.scaleX = 1f
        binding.outerCircle.scaleY = 1f
        binding.innerCircle.scaleX = 1f
        binding.innerCircle.scaleY = 1f
    }
    
    private fun startTripRequestTimer() {
        tripRequestRunnable = object : Runnable {
            override fun run() {
                if (isOnline && !isShowingTripRequest) {
                    showTripRequestBottomSheet()
                }
                tripRequestHandler.postDelayed(this, 5000) // 5 seconds
            }
        }
        tripRequestHandler.postDelayed(tripRequestRunnable!!, 5000)
    }
    
    private fun stopTripRequestTimer() {
        tripRequestRunnable?.let {
            tripRequestHandler.removeCallbacks(it)
        }
        tripRequestRunnable = null
    }
    
    private fun showTripRequestBottomSheet() {
        // Prevent showing multiple bottom sheets
        if (isShowingTripRequest) return
        
        isShowingTripRequest = true
        val bottomSheet = TripRequestBottomSheet.newInstance()
        
        bottomSheet.setOnAcceptListener {
            isShowingTripRequest = false
            Toast.makeText(context, "Order accepted!", Toast.LENGTH_SHORT).show()
            
            // Stop the trip request timer
            stopTripRequestTimer()
            
            // Navigate to pickup arrival screen
            findNavController().navigate(R.id.action_driverHome_to_pickupArrival)
        }
        
        bottomSheet.setOnDeclineListener {
            isShowingTripRequest = false
            Toast.makeText(context, "Order declined", Toast.LENGTH_SHORT).show()
        }
        
        bottomSheet.setOnTimeoutListener {
            isShowingTripRequest = false
            Toast.makeText(context, "Request timeout", Toast.LENGTH_SHORT).show()
        }
        
        // Ensure flag is reset even if dialog is dismissed unexpectedly
        bottomSheet.setOnDismissListener {
            isShowingTripRequest = false
        }
        
        bottomSheet.show(childFragmentManager, "TripRequestBottomSheet")
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        stopPulseAnimations()
        stopTripRequestTimer()
        isShowingTripRequest = false
    }
}
