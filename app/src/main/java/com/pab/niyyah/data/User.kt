package com.pab.niyyah.data

/**
 * Data class untuk menyimpan informasi user
 * Document ID dari Firestore digunakan sebagai unique identifier
 */
data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val username: String = "",
    val gender: String = "",
    val nationality: String = "",
    val birthDate: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = ""
) {
    /**
     * Helper untuk mendapatkan full name
     */
    val fullName: String
        get() = "$firstName $lastName".trim()
}
