package com.grex.vyay

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class VyayApp : Application() {
    companion object {
        lateinit var instance: VyayApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver())
    }
}

class AppLifecycleObserver : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val smsAnalysisService = SmsAnalysisService.getInstance()
        when (event) {
            Lifecycle.Event.ON_START -> {
                // App came to foreground, now check for new SMS changes
                smsAnalysisService.startAnalysis()
            }

            Lifecycle.Event.ON_DESTROY -> {
                // App is about to be destroyed, do cleanup
                smsAnalysisService.stopAnalysis()
            }

            else -> {}
        }

    }
}