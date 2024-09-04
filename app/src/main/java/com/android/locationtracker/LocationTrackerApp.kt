package com.android.locationtracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.android.locationtracker.LocationService.Companion.LOCATION_CHANNEL_ID
import com.android.locationtracker.LocationService.Companion.LOCATION_CHANNEL_NAME

class LocationTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initLocationChannel()
    }

    private fun initLocationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LOCATION_CHANNEL_ID,
                LOCATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}