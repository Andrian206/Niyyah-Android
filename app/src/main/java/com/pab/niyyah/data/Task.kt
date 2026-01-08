package com.pab.niyyah.data

data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val details: String = "",
    val dueDate: String = "",
    val time: String = "",
    val repeat: String = "Never",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0
)
