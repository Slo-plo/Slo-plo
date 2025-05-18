package com.example.slo_plo.model

data class TrashBin(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val title: String = "",
    val description: String? = null
)