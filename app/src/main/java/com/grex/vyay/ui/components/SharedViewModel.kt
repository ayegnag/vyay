package com.grex.vyay.ui.components

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grex.vyay.SharedViewModelInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application),
    SharedViewModelInterface {
    private val sharedPreferences: SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(application)

    private val _prefExpenseThreshold = MutableStateFlow(0.0)
    override val prefExpenseThreshold = _prefExpenseThreshold.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        Log.d("PreferenceChangeListener", "Preference changed: $key")
        if (key == PREF_MONTHLY_EXPENSE_LIMIT) {
            updateExpenseThreshold()
        }
    }

    init {
        observeSharedPreferences()
        updateExpenseThreshold()
    }

    private fun observeSharedPreferences() {
        Log.d("PreferenceChangeListener", "Initialized")
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun updateExpenseThreshold() {
        viewModelScope.launch {
            _prefExpenseThreshold.value = sharedPreferences.getFloat(PREF_MONTHLY_EXPENSE_LIMIT, 0.0f).toDouble()
        }
    }

    override fun setExpenseThreshold(value: Double) {
        viewModelScope.launch {
            sharedPreferences.edit().putFloat(PREF_MONTHLY_EXPENSE_LIMIT, value.toFloat()).apply()
            // The preferenceChangeListener will trigger updateExpenseThreshold()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    companion object {
        const val PREF_MONTHLY_EXPENSE_LIMIT = "preference_monthly_expense_limit"
    }
}