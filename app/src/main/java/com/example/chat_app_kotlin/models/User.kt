package com.example.chat_app_kotlin.models

import com.google.firebase.firestore.FieldValue

data class User(
        val name:String,
        val imageUrl:String,
        val thumbImage:String,
        val uid:String,
        val deviceToken:String,
        val status:String,
        val onlineStatus: String) {
    // For making a class for firebase we have to make a empty constructor
    constructor():this ("","","","","","","")
    constructor(name: String,imageUrl: String,thumbImage: String,uid: String):this (name,
        imageUrl,
        thumbImage,
        uid,
        "",
        "Hey there i am using whatsapp",
        ""
    )
}