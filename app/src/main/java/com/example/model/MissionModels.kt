package com.example.model

enum class MissionMetric {
    NEAR_MISSES,
    DRAFT_SECONDS,
    TWO_WAY_METERS,
    PICKUPS_COLLECTED,
    TOP_SPEED_KMH,
    RUNS_COMPLETED
}

data class DailyMission(
    val id: String,
    val title: String,
    val description: String,
    val metric: MissionMetric,
    val target: Int,
    val rewardUsd: Int,
    val progress: Int = 0,
    val claimed: Boolean = false
) {
    val isComplete: Boolean get() = progress >= target
}

data class CareerMilestone(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val rewardUsd: Int,
    val progress: Int = 0,
    val claimed: Boolean = false
) {
    val isComplete: Boolean get() = progress >= target
}

data class LifetimeStats(
    val totalUsdEarned: Int = 0,
    val totalNearMisses: Int = 0,
    val totalDraftSeconds: Int = 0,
    val totalTwoWayMeters: Int = 0,
    val totalPickups: Int = 0,
    val highestSpeedKmh: Int = 0,
    val totalRuns: Int = 0
)

object MissionCatalog {
    private val pool = listOf(
        DailyMission(
            id = "m_near_12",
            title = "Close Shave",
            description = "Trigger 12 near-miss passes",
            metric = MissionMetric.NEAR_MISSES,
            target = 12,
            rewardUsd = 650
        ),
        DailyMission(
            id = "m_draft_10",
            title = "Slipstream Shadow",
            description = "Draft behind traffic for 10 seconds",
            metric = MissionMetric.DRAFT_SECONDS,
            target = 10,
            rewardUsd = 700
        ),
        DailyMission(
            id = "m_twoway_2000",
            title = "Wrong-Way Daredevil",
            description = "Ride 2,000 m in Two-Way mode",
            metric = MissionMetric.TWO_WAY_METERS,
            target = 2000,
            rewardUsd = 950
        ),
        DailyMission(
            id = "m_pickups_8",
            title = "Highway Scavenger",
            description = "Grab 8 USD, Nitro, or Shield pickups",
            metric = MissionMetric.PICKUPS_COLLECTED,
            target = 8,
            rewardUsd = 600
        ),
        DailyMission(
            id = "m_speed_260",
            title = "Redline Blur",
            description = "Hit 260 km/h in a single run",
            metric = MissionMetric.TOP_SPEED_KMH,
            target = 260,
            rewardUsd = 850
        ),
        DailyMission(
            id = "m_runs_3",
            title = "Highway Regular",
            description = "Complete 3 highway runs",
            metric = MissionMetric.RUNS_COMPLETED,
            target = 3,
            rewardUsd = 550
        )
    )

    fun dailyMissionsForDay(epochDay: Long): List<DailyMission> {
        val start = ((epochDay % pool.size) + pool.size).toInt() % pool.size
        return listOf(
            pool[start],
            pool[(start + 2) % pool.size],
            pool[(start + 4) % pool.size]
        )
    }

    fun buildMilestones(
        stats: LifetimeStats,
        ownedBikeCount: Int,
        claimedIds: Set<String>
    ): List<CareerMilestone> = listOf(
        CareerMilestone(
            id = "ms_usd_10k",
            title = "Street Hustler",
            description = "Earn $10,000 lifetime prize USD",
            target = 10000,
            rewardUsd = 1500,
            progress = stats.totalUsdEarned,
            claimed = "ms_usd_10k" in claimedIds
        ),
        CareerMilestone(
            id = "ms_near_50",
            title = "Apex Weaver",
            description = "Land 50 career near misses",
            target = 50,
            rewardUsd = 1800,
            progress = stats.totalNearMisses,
            claimed = "ms_near_50" in claimedIds
        ),
        CareerMilestone(
            id = "ms_twoway_10k",
            title = "Headlight Runner",
            description = "Ride 10,000 m in Two-Way oncoming traffic",
            target = 10000,
            rewardUsd = 2500,
            progress = stats.totalTwoWayMeters,
            claimed = "ms_twoway_10k" in claimedIds
        ),
        CareerMilestone(
            id = "ms_bikes_3",
            title = "Garage Collector",
            description = "Own 3 real-world motorcycles",
            target = 3,
            rewardUsd = 2200,
            progress = ownedBikeCount,
            claimed = "ms_bikes_3" in claimedIds
        ),
        CareerMilestone(
            id = "ms_speed_320",
            title = "320 KM/H Club",
            description = "Clock 320 km/h top speed",
            target = 320,
            rewardUsd = 3500,
            progress = stats.highestSpeedKmh,
            claimed = "ms_speed_320" in claimedIds
        )
    )
}
