package com.example.project

data class Place(
    val name : String,
    val id : String,
    val rating : Double,
    val reviewsCount : Int,
    val score : Int,
    val imageName : String,
    val address : String,
    val gMapsUrl : String
)
