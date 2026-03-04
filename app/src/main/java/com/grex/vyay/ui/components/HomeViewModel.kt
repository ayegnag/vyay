package com.grex.vyay.ui.components

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {
    private val _prefUpdateFlag = MutableStateFlow(false)
    private val _prefProcessingFlag = MutableStateFlow(false)
    val prefSimilarRecordUpdateFlag = _prefUpdateFlag.asStateFlow()
    val prefProcessingSimilarRecordFlag = _prefProcessingFlag.asStateFlow()

    init {
        observeSharedPreferences()
    }

    private fun observeSharedPreferences() {
        Log.d("PreferenceChangeListener", "Initialized")
        Log.d("PreferenceChangeListener", sharedPreferences.toString())
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            Log.d("PreferenceChangeListener", key.toString())
            if (key == "show_updateSimilarRecords_banner") {
                viewModelScope.launch {
                    _prefUpdateFlag.value = true
                }
            }
            if (key == "show_processingSimilarRecords_banner") {
                viewModelScope.launch {
                    _prefProcessingFlag.value = true
                }
            }
        }
    }

    fun commitPrefProcessingFlag() {
        _prefProcessingFlag.value = true
    }
    fun resetPrefProcessingFlag() {
        _prefProcessingFlag.value = false
    }

    fun commitPrefUpdateFlag() {
        _prefUpdateFlag.value = true
    }
    // Reset the flag after showing the update
    fun resetPrefUpdateFlag() {
        _prefUpdateFlag.value = false
    }
}