package com.example.chatapp.notifications

data class Data(
    val user: String,
    val icon: Int,
    val body: String,
    val title: String,
    val sented: String,
    val groupKey:String?=null,
    val id:Int
)
