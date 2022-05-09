package com.example.chatapp.models

import java.io.Serializable


class User : Serializable {
    var uid:String?=null
    var displayName:String?=null
    var photoUrl:String?=null
    var phoneNumber:String?=null
    var email:String?=null
    var status:String?=null

    constructor(
        uid: String?,
        displayName: String?,
        photoUrl: String?,
        phoneNumber: String?,
        email: String?,
        status: String?,
    ) {
        this.uid = uid
        this.displayName = displayName
        this.photoUrl = photoUrl
        this.phoneNumber = phoneNumber
        this.email = email
        this.status = status
    }

    constructor()

}