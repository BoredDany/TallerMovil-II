package com.example.tallermovil_ii.model

class User {
    var name: String
    var lastName: String
    var email: String
    var password: String
    var profilePhoto: String
    var identification: String
    var latitude: Double
    var longitude: Double

    constructor(
        name: String,
        lastName: String,
        email: String,
        password: String,
        profilePhoto: String,
        identification: String,
        latitude: Double,
        longitude: Double
    ) {
        this.name = name
        this.lastName = lastName
        this.email = email
        this.password = password
        this.profilePhoto = profilePhoto
        this.identification = identification
        this.latitude = latitude
        this.longitude = longitude
    }
}