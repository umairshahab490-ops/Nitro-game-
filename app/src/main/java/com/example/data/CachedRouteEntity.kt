package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_routes")
data class CachedRouteEntity(
    @PrimaryKey val id: String,
    val roadName: String,
    val regionLabel: String,
    val roadClass: String,
    val waypointsJson: String,
    val curvatureJson: String,
    val lengthMeters: Int,
    val isGpsDerived: Boolean,
    val savedAtMillis: Long = System.currentTimeMillis()
)
