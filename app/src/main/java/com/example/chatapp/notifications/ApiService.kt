package com.example.chatapp.notifications

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call

interface ApiService {

    @Headers(
        "Content-type:application/json",
        "Authorization:key=AAAA0N1pMXE:APA91bHPPu5XyM9bXYsSjbqTxE0OI0Y-8Hu9J0dnswTPY2QGp6RXyPNbqFREAJYcl-zw59M7ybTZWS7tdnRcdcnDabIE8vfGPtq--qG73ODpP3XgdmxXWLRZfZXIGo7CD_6v6EhDLxiB"
    )
    @POST("fcm/send")
    fun sendNotification(@Body sender: Sender): Call<MyResponce>

}