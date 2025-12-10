package com.pab.niyyah

data class User(
    val idUser: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val photoUrl: String = ""
)
