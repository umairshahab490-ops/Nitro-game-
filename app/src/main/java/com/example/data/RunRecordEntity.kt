package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_records")
data class RunRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val distanceMeters: Int,
    val topSpeedKmh: Int,
    val nearMisses: Int,
    val usdEarned: Int,
    val bikeName: String,
    val routeName: String,
    val difficulty: String,
    val gameMode: String,
    val trafficMode: String,
    val endReason: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
