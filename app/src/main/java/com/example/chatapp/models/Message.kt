package com.example.chatapp.models

class Message {
    var text:String?=null
    var date:String?=null
    var fromUserId:String?=null

    constructor()
    constructor(text: String?, date: String?, fromUserId: String?) {
        this.text = text
        this.date = date
        this.fromUserId = fromUserId
    }
}