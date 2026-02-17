package com.example.logistics_driver_app.modules.tripModule.view.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.logistics_driver_app.R
import com.example.logistics_driver_app.databinding.BottomSheetTripRequestBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TripRequestBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTripRequestBinding? = null
    private val binding get() = _binding!!
    
    private var countDownTimer: CountDownTimer? = null
    private var onAcceptListener: (() -> Unit)? = null
    private var onDeclineListener: (() -> Unit)? = null
    private var onTimeoutListener: (() -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var hasUserResponded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTripRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isCancelable = false
        
        setupViews()
        startTimer()
        setupClickListeners()
    }

    private fun setupViews() {
        // Set data from arguments
        arguments?.let { args ->
            binding.tvPickupAddress.text = args.getString(ARG_PICKUP_ADDRESS, "Anna Nagar, Chennai")
            binding.tvPickupDistance.text = args.getString(ARG_PICKUP_DISTANCE, "1.2 km away")
            binding.tvDropAddress.text = args.getString(ARG_DROP_ADDRESS, "T. Nagar, Chennai")
            binding.tvTripDistance.text = args.getString(ARG_TRIP_DISTANCE, "~8.5 km trip")
            binding.tvPackageType.text = args.getString(ARG_PACKAGE_TYPE, "Documents")
            binding.tvPackageWeight.text = args.getString(ARG_PACKAGE_WEIGHT, "2 kg")
            binding.tvEstimatedFare.text = "₹${args.getInt(ARG_ESTIMATED_FARE, 350)}"
            
            val helperRequired = args.getBoolean(ARG_HELPER_REQUIRED, false)
            binding.tvHelperBanner.visibility = if (helperRequired) View.VISIBLE else View.GONE
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.tvTimer.text = "${secondsLeft}s"
            }

            override fun onFinish() {
                hasUserResponded = true
                onTimeoutListener?.invoke()
                dismiss()
            }
        }.start()
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            countDownTimer?.cancel()
            hasUserResponded = true
            onAcceptListener?.invoke()
            dismiss()
        }

        binding.btnDecline.setOnClickListener {
            countDownTimer?.cancel()
            hasUserResponded = true
            onDeclineListener?.invoke()
            dismiss()
        }
    }

    fun setOnAcceptListener(listener: () -> Unit) {
        onAcceptListener = listener
    }

    fun setOnDeclineListener(listener: () -> Unit) {
        onDeclineListener = listener
    }

    fun setOnTimeoutListener(listener: () -> Unit) {
        onTimeoutListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }

    companion object {
        private const val ARG_PICKUP_ADDRESS = "pickup_address"
        private const val ARG_PICKUP_DISTANCE = "pickup_distance"
        private const val ARG_DROP_ADDRESS = "drop_address"
        private const val ARG_TRIP_DISTANCE = "trip_distance"
        private const val ARG_PACKAGE_TYPE = "package_type"
        private const val ARG_PACKAGE_WEIGHT = "package_weight"
        private const val ARG_ESTIMATED_FARE = "estimated_fare"
        private const val ARG_HELPER_REQUIRED = "helper_required"

        fun newInstance(
            pickupAddress: String = "Anna Nagar, Chennai",
            pickupDistance: String = "1.2 km away",
            dropAddress: String = "T. Nagar, Chennai",
            tripDistance: String = "~8.5 km trip",
            packageType: String = "Documents",
            packageWeight: String = "2 kg",
            estimatedFare: Int = 350,
            helperRequired: Boolean = false
        ): TripRequestBottomSheet {
            return TripRequestBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_PICKUP_ADDRESS, pickupAddress)
                    putString(ARG_PICKUP_DISTANCE, pickupDistance)
                    putString(ARG_DROP_ADDRESS, dropAddress)
                    putString(ARG_TRIP_DISTANCE, tripDistance)
                    putString(ARG_PACKAGE_TYPE, packageType)
                    putString(ARG_PACKAGE_WEIGHT, packageWeight)
                    putInt(ARG_ESTIMATED_FARE, estimatedFare)
                    putBoolean(ARG_HELPER_REQUIRED, helperRequired)
                }
            }
        }
    }
}
