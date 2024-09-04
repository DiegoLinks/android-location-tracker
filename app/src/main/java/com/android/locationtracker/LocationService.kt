package com.android.locationtracker

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient
    private val interval = 300000L//5 minutos

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val LOCATION_CHANNEL_ID = "location"
        const val LOCATION_CHANNEL_NAME = "Localização"
        const val LOCATION_SERVICE_NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        locationClient = LocationClientImpl(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun start() {
        locationClient.getLocationUpdates(interval = interval)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.i(
                    "Enviando localização para o servidor...",
                    "Location: ${location.latitude}, ${location.longitude}"
                )
            }.launchIn(serviceScope)

        startForeground(
            LOCATION_SERVICE_NOTIFICATION_ID,
            getNotification(),
            FOREGROUND_SERVICE_TYPE_LOCATION
        )
    }

    /**
     * Apesar dessa notificação nunca ser enviada ao usuário, ela precisa estar configurada corretamente
     * para que seja mantida pelo sistema e execute o fluxo de envio periódico da localização.
     */
    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, LOCATION_CHANNEL_ID)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}