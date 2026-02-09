package com.example.logistics_driver_app.modules.loginModule.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * BaseViewModel - Base class for all ViewModels in the application.
 * Provides common functions for API calls and exception handling.
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    protected val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    protected val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Exception handler for coroutines.
     * Handles uncaught exceptions in coroutine scope.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    /**
     * Handle exceptions from coroutines.
     * Override to provide custom exception handling.
     * @param throwable Thrown exception
     */
    protected open fun handleException(throwable: Throwable) {
        throwable.printStackTrace()
        _error.postValue(throwable.message)
        // Log to crashlytics or analytics
    }

    /**
     * Launch coroutine with exception handling.
     * Use this for all async operations in ViewModels.
     * @param block Suspend function to execute
     */
    protected fun launchCoroutine(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }
}
