package com.pab.niyyah.data
import com.google.firebase.firestore.PropertyName

data class Task(
    var id: String = "",
    var userId: String = "",
    var title: String = "",
    var details: String = "",
    var dueDate: String = "",
    var time: String = "",
    var repeat: String = "Never",

    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    var createdAt: Long = 0
)
