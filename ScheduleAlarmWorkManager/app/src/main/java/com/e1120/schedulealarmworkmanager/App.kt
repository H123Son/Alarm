package com.e1120.schedulealarmworkmanager

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import io.chanjungkim.alarm_workmanager.logdd

class App: Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()

        setupLifecycleListener()
    }
    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        logdd("My_Lifecycle: Returning to foreground…")
        isBackground = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        logdd("My_Lifecycle: Moving to background…")
        isBackground = true
    }

    companion object {
        var isBackground = true
    }
}