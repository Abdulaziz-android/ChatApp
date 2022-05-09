package com.example.chatapp.models

import java.io.Serializable

class Group : Serializable {
    var name: String? = null
    var key: String? = null

    constructor(name: String, key: String) {
        this.name = name
        this.key = key
    }

    constructor()
}