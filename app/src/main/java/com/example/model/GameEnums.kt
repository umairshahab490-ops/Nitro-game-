package com.example.model

enum class GameState {
    MENU,
    COUNTDOWN,
    RUNNING,
    PAUSED,
    DYING,
    OVER
}

enum class Difficulty(
    val label: String,
    val trafficCount: Int,
    val startSeconds: Float,
    val checkpointBonus: Float,
    val centrifugal: Float,
    val baseGrip: Float,
    val trafficMaxRatio: Float,
    val usdMultiplier: Float
) {
    CHILL(
        label = "CHILL",
        trafficCount = 16,
        startSeconds = 50f,
        checkpointBonus = 30f,
        centrifugal = 0.28f,
        baseGrip = 1.0f,
        trafficMaxRatio = 0.62f,
        usdMultiplier = 1.0f
    ),
    PRO(
        label = "PRO",
        trafficCount = 26,
        startSeconds = 42f,
        checkpointBonus = 24f,
        centrifugal = 0.32f,
        baseGrip = 1.0f,
        trafficMaxRatio = 0.72f,
        usdMultiplier = 1.35f
    ),
    INSANE(
        label = "INSANE",
        trafficCount = 36,
        startSeconds = 34f,
        checkpointBonus = 19f,
        centrifugal = 0.36f,
        baseGrip = 0.92f,
        trafficMaxRatio = 0.80f,
        usdMultiplier = 1.75f
    )
}

enum class GameMode(val label: String, val subtitle: String) {
    TIME_ATTACK("TIME ATTACK", "Beat checkpoints before 0s"),
    ENDLESS("ENDLESS", "Survive 3 lives, chase distance")
}

enum class TrafficMode(val label: String, val usdMultiplier: Float) {
    ONE_WAY("ONE-WAY", 1.0f),
    TWO_WAY("TWO-WAY 2×$", 2.0f)
}

enum class PickupType {
    USD_CASH,
    NITRO_CELL,
    SHIELD
}

enum class VehicleType {
    TRUCK,
    CAR,
    BIKE
}
