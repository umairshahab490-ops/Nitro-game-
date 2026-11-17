package com.example.model

data class BikeModel(
    val id: String,
    val manufacturer: String,
    val modelName: String,
    val year: Int,
    val msrpUsd: Int,
    val displacementCc: Int,
    val category: String,
    val tagline: String,
    val baseTopSpeedMult: Float,
    val baseAccelMult: Float,
    val baseGripMult: Float,
    val baseNitroMult: Float,
    val payoutBonus: Float,
    val primaryColor: Int,
    val accentColor: Int,
    val trimColor: Int
) {
    val fullName: String get() = "$manufacturer $modelName"
}

data class BikeUpgradeState(
    val engineLevel: Int = 1,
    val tiresLevel: Int = 1,
    val nitroLevel: Int = 1
) {
    val totalUpgradeLevel: Int get() = engineLevel + tiresLevel + nitroLevel
}

enum class UpgradeStat(val label: String) {
    ENGINE("ENGINE"),
    TIRES("TIRES"),
    NITRO("NITRO")
}

object BikeCatalog {
    const val MAX_UPGRADE_LEVEL = 5

    val bikes: List<BikeModel> = listOf(
        BikeModel(
            id = "kawasaki_ninja_500",
            manufacturer = "Kawasaki",
            modelName = "Ninja 500",
            year = 2025,
            msrpUsd = 5499,
            displacementCc = 451,
            category = "SPORT TWIN",
            tagline = "Starter sportbike with nimble lane flickability.",
            baseTopSpeedMult = 1.00f,
            baseAccelMult = 1.00f,
            baseGripMult = 1.00f,
            baseNitroMult = 1.00f,
            payoutBonus = 1.00f,
            primaryColor = 0xFF39D353.toInt(),
            accentColor = 0xFF22E6FF.toInt(),
            trimColor = 0xFF198A2E.toInt()
        ),
        BikeModel(
            id = "honda_cbr650r",
            manufacturer = "Honda",
            modelName = "CBR650R",
            year = 2025,
            msrpUsd = 9199,
            displacementCc = 649,
            category = "INLINE-4 SPORT",
            tagline = "High-revving 4-cylinder punch and poised stability.",
            baseTopSpeedMult = 1.07f,
            baseAccelMult = 1.08f,
            baseGripMult = 1.04f,
            baseNitroMult = 1.05f,
            payoutBonus = 1.12f,
            primaryColor = 0xFFE5253A.toInt(),
            accentColor = 0xFFFFD34D.toInt(),
            trimColor = 0xFF9E1424.toInt()
        ),
        BikeModel(
            id = "yamaha_yzf_r7",
            manufacturer = "Yamaha",
            modelName = "YZF-R7",
            year = 2025,
            msrpUsd = 9399,
            displacementCc = 689,
            category = "SUPERSPORT",
            tagline = "Razor-sharp cornering grip and fast Nitro recovery.",
            baseTopSpeedMult = 1.09f,
            baseAccelMult = 1.10f,
            baseGripMult = 1.14f,
            baseNitroMult = 1.12f,
            payoutBonus = 1.18f,
            primaryColor = 0xFF1A6BFF.toInt(),
            accentColor = 0xFF22E6FF.toInt(),
            trimColor = 0xFF0E3E99.toInt()
        ),
        BikeModel(
            id = "bmw_s1000rr",
            manufacturer = "BMW",
            modelName = "S 1000 RR",
            year = 2025,
            msrpUsd = 18995,
            displacementCc = 999,
            category = "SUPERBIKE",
            tagline = "ShiftCam litre weapon with explosive 6-gear pull.",
            baseTopSpeedMult = 1.18f,
            baseAccelMult = 1.20f,
            baseGripMult = 1.16f,
            baseNitroMult = 1.15f,
            payoutBonus = 1.32f,
            primaryColor = 0xFFE8EEF8.toInt(),
            accentColor = 0xFF2B7FFF.toInt(),
            trimColor = 0xFFFF2D78.toInt()
        ),
        BikeModel(
            id = "suzuki_hayabusa",
            manufacturer = "Suzuki",
            modelName = "Hayabusa",
            year = 2025,
            msrpUsd = 19499,
            displacementCc = 1340,
            category = "HYPERBIKE",
            tagline = "1,340cc aero missile built for endless Nitro runs.",
            baseTopSpeedMult = 1.23f,
            baseAccelMult = 1.17f,
            baseGripMult = 1.12f,
            baseNitroMult = 1.28f,
            payoutBonus = 1.38f,
            primaryColor = 0xFF9AA7BD.toInt(),
            accentColor = 0xFFFF8A2B.toInt(),
            trimColor = 0xFF556072.toInt()
        ),
        BikeModel(
            id = "ducati_panigale_v4s",
            manufacturer = "Ducati",
            modelName = "Panigale V4 S",
            year = 2025,
            msrpUsd = 33510,
            displacementCc = 1103,
            category = "FLAGSHIP V4",
            tagline = "MotoGP-derived Desmosedici power and max USD bonus.",
            baseTopSpeedMult = 1.28f,
            baseAccelMult = 1.26f,
            baseGripMult = 1.22f,
            baseNitroMult = 1.26f,
            payoutBonus = 1.55f,
            primaryColor = 0xFFFF2D46.toInt(),
            accentColor = 0xFFFFD34D.toInt(),
            trimColor = 0xFFA61224.toInt()
        )
    )

    fun findById(id: String): BikeModel = bikes.firstOrNull { it.id == id } ?: bikes.first()

    fun withPaint(bike: BikeModel, paintId: String?): BikeModel {
        val swatch = BikePaintCatalog.findById(paintId) ?: return bike
        return if (swatch.id == BikePaintCatalog.STOCK_ID) {
            bike
        } else {
            bike.copy(
                primaryColor = swatch.primaryColor,
                trimColor = swatch.trimColor,
                accentColor = swatch.accentColor
            )
        }
    }

    fun tierAndRating(bike: BikeModel, upgrades: BikeUpgradeState): Pair<String, Float> {
        val avgStat = (
            effectiveTopSpeedMult(bike, upgrades) +
                effectiveAccelMult(bike, upgrades) +
                effectiveGripMult(bike, upgrades) +
                effectiveNitroRechargeMult(bike, upgrades)
            ) / 4f
        val rating = (3.4f + (avgStat - 1.0f) * 3.1f).coerceIn(3.5f, 5.0f)
        val tier = when {
            avgStat >= 1.34f -> "S+"
            avgStat >= 1.22f -> "S"
            avgStat >= 1.12f -> "A"
            else -> "B"
        }
        return tier to rating
    }

    fun upgradeCostUsd(bike: BikeModel, currentLevel: Int): Int {
        if (currentLevel >= MAX_UPGRADE_LEVEL) return 0
        val tierScale = (bike.msrpUsd / 5499f).coerceIn(1f, 2.6f)
        val baseCost = when (currentLevel) {
            1 -> 650
            2 -> 1350
            3 -> 2500
            else -> 4200
        }
        return ((baseCost * tierScale) / 50f).toInt() * 50
    }

    fun effectiveTopSpeedMult(bike: BikeModel, upgrades: BikeUpgradeState): Float =
        bike.baseTopSpeedMult + (upgrades.engineLevel - 1) * 0.04f

    fun effectiveAccelMult(bike: BikeModel, upgrades: BikeUpgradeState): Float =
        bike.baseAccelMult + (upgrades.engineLevel - 1) * 0.06f

    fun effectiveGripMult(bike: BikeModel, upgrades: BikeUpgradeState): Float =
        bike.baseGripMult + (upgrades.tiresLevel - 1) * 0.055f

    fun effectiveNitroRechargeMult(bike: BikeModel, upgrades: BikeUpgradeState): Float =
        bike.baseNitroMult + (upgrades.nitroLevel - 1) * 0.07f

    fun effectiveNitroBurnRateScale(upgrades: BikeUpgradeState): Float =
        1f - (upgrades.nitroLevel - 1) * 0.08f
}

data class BikePaintSwatch(
    val id: String,
    val label: String,
    val primaryColor: Int,
    val trimColor: Int,
    val accentColor: Int
)

object BikePaintCatalog {
    const val STOCK_ID = "stock"

    val swatches: List<BikePaintSwatch> = listOf(
        BikePaintSwatch(
            id = STOCK_ID,
            label = "FACTORY",
            primaryColor = 0xFF64748B.toInt(),
            trimColor = 0xFF334155.toInt(),
            accentColor = 0xFF22E6FF.toInt()
        ),
        BikePaintSwatch(
            id = "crimson_track",
            label = "CRIMSON",
            primaryColor = 0xFFE81C5A.toInt(),
            trimColor = 0xFF8A0D32.toInt(),
            accentColor = 0xFF22E6FF.toInt()
        ),
        BikePaintSwatch(
            id = "venom_lime",
            label = "VENOM LIME",
            primaryColor = 0xFF39D353.toInt(),
            trimColor = 0xFF167A27.toInt(),
            accentColor = 0xFFFFD34D.toInt()
        ),
        BikePaintSwatch(
            id = "electric_cyan",
            label = "CYBER CYAN",
            primaryColor = 0xFF19D4EA.toInt(),
            trimColor = 0xFF0A6A80.toInt(),
            accentColor = 0xFFEEF2FF.toInt()
        ),
        BikePaintSwatch(
            id = "midnight_stealth",
            label = "STEALTH",
            primaryColor = 0xFF2E374A.toInt(),
            trimColor = 0xFF141923.toInt(),
            accentColor = 0xFFE81C5A.toInt()
        ),
        BikePaintSwatch(
            id = "solar_gold",
            label = "SOLAR GOLD",
            primaryColor = 0xFFFFB020.toInt(),
            trimColor = 0xFF995A00.toInt(),
            accentColor = 0xFF22E6FF.toInt()
        )
    )

    fun findById(id: String?): BikePaintSwatch? =
        swatches.firstOrNull { it.id == id }
}
