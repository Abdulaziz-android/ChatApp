package com.example.chatapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.chatapp.MainActivity
import com.example.chatapp.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var sPref: SharedPreferences

    companion object {
        const val CHANNEL_1_ID = "channel1"
        const val NOTIFICATION_ID = 1
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            updateToken(token)
        }
    }

    private fun updateToken(refreshToken: String) {

        val user = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(refreshToken)
        reference.child(user!!.uid).setValue(token)

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sPref = getSharedPreferences("shared", MODE_PRIVATE)

        if (remoteMessage.getData().size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOreoNotification(remoteMessage)
            } else {
                sendNotification(remoteMessage)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val groupKey = remoteMessage.data["groupKey"]
        val id = remoteMessage.data["id"]

        var isUser = true
        val backIntent = Intent(this, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val notification = remoteMessage.notification
        val j = user?.replace(Regex("[\\D]"), "")?.toInt()!!
        val d = j.plus(id!!.toInt())
        val intent = Intent(this, MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putBoolean("notification", true)
        if (title != "New Message:") {
            bundle.putString("group", groupKey)
            isUser = false
        } else bundle.putString("userid", user)
        bundle.putInt("notiID", j)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivities(this, d, arrayOf(backIntent, intent), PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)
        val builder = oreoNotification.getOreoNotification(title!!,
            body!!,
            pendingIntent,
            defaultSound,
            icon!!)
        builder
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        var i = 0
        if (j > 0) {
            i = j
        }

        val str = sPref.getString("current", "null")


        if (str != user && isUser)
            oreoNotification.getManager().notify(i, builder.build())
        else if (str != groupKey && !isUser)
            oreoNotification.getManager().notify(i, builder.build())


    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data.get("user")
        val icon = remoteMessage.data.get("icon")
        val title = remoteMessage.data.get("title")
        val body = remoteMessage.data.get("body")
        val groupKey = remoteMessage.data["groupKey"]
        val id = remoteMessage.data["id"]

        var isUser = true
        val backIntent = Intent(this, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)


        val notification = remoteMessage.notification
        val j = user?.replace(Regex("[\\D]"), "")?.toInt()!!
        val d = j.plus(id!!.toInt())

        val intent = Intent(this, MainActivity::class.java)
        val bundle = Bundle()
        bundle.putBoolean("notification", true)
        if (title != "New Message:") {
            bundle.putString("group", groupKey)
            isUser = false
        } else bundle.putString("userid", user)
        bundle.putInt("notiID", j)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivities(this, d, arrayOf(backIntent, intent), PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this, CHANNEL_1_ID)
            .setSmallIcon(icon!!.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setSound(defaultSound)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val noti =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        var i = 0
        if (j > 0) {
            i = j
        }
        val str = sPref.getString("current", "null")
        if (str != user && isUser)
            noti.notify(i, builder.build())
        else if (str != groupKey && !isUser)
            noti.notify(i, builder.build())
    }

}