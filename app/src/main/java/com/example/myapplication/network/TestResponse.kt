package com.example.myapplication.network

import com.google.gson.annotations.SerializedName

data class TestResponse(
    @SerializedName("allowed") val allowed: Boolean
)