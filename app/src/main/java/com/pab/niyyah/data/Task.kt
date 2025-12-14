package com.pab.niyyah.data

data class Task(
    val id: String = "",
    val title: String = "",
    val details: String = "",
    val date: Long = 0,
    val time: String = "",
    val repeat: String = "",
    val isCompleted: Boolean = false
)
