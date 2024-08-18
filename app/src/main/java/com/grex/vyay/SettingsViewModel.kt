package com.grex.vyay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(private val appDao: AppDao) : ViewModel() {
    fun deleteAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            appDao.deleteAllMessages()
        }
    }
}