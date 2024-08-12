package com.grex.vyay

import android.app.Application

class VyayApp : Application() {
    companion object {
        lateinit var instance: VyayApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}