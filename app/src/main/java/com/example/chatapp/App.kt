package com.example.chatapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class App : Application() {
    companion object {
        const val CHANNEL_1_ID = "channel1"
        const val NOTIFICATION_ID = 1
    }
        override fun onCreate() {
        super.onCreate()

        createNotificationChannesl()
    }

    private fun createNotificationChannesl() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "channel name"
            val description = "channel description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_1_ID, name, importance)
            channel.description = description

            val notificationManager = getSystemService(
                NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}