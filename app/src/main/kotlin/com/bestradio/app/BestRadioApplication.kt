package com.bestradio.app

import android.app.Application
import com.bestradio.app.di.AppContainer

class BestRadioApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
