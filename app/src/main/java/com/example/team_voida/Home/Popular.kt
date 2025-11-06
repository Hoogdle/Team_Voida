package com.example.team_voida.Home

import kotlinx.serialization.Serializable

@Serializable
data class Popular(
    val id: Int,
    val name: String,
    val description: String,
    val price: Float,
    val image_url: String,
    val category: String,
    val sector: Int
)
