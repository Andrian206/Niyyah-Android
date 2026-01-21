package com.pab.niyyah.data

/**
 * Data class untuk menyimpan informasi user
 * Document ID dari Firestore digunakan sebagai unique identifier
 */
data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var username: String = "",
    var gender: String = "",
    var nationality: String = "",
    var birthDate: String = "",
    var phoneNumber: String = "",
    var photoUrl: String = ""
) {
    /**
     * Helper untuk mendapatkan full name
     */
    val fullName: String
        get() = "$firstName $lastName".trim()
}
