package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

data class TestRequest(
    @SerializedName("gender") val gender: String,
    @SerializedName("age") val age: Int,
)