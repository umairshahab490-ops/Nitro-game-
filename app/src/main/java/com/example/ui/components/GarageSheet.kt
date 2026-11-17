package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TireRepair
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlayerPreferencesSnapshot
import com.example.game.GameEngine
import com.example.model.BikeCatalog
import com.example.model.BikeModel
import com.example.model.BikePaintCatalog
import com.example.model.BikeUpgradeState
import com.example.model.UpgradeStat
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CrimsonPlayEnd
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlueEnd
import com.example.ui.theme.ElectricBlueStart
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SelectedGreenEnd
import com.example.ui.theme.SelectedGreenStart
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

private enum class GarageTab(val label: String, val tag: String) {
    BIKES("BIKES", "tab_bikes"),
    UPGRADES("UPGRADES", "tab_upgrades"),
    PAINT("PAINT", "tab_paint")
}

@Composable
fun GarageSheet(
    prefs: PlayerPreferencesSnapshot,
    onBuyBike: (BikeModel) -> Unit,
    onEquipBike: (String) -> Unit,
    onUpgradeBike: (BikeModel, UpgradeStat) -> Unit,
    onSelectPaint: (bikeId: String, paintId: String) -> Unit = { _, _ -> },
    onNavigateTab: (PaddockNavTab) -> Unit = {},
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var selectedBikeId by remember(prefs.equippedBikeId) { mutableStateOf(prefs.equippedBikeId) }
    var activeTab by remember { mutableStateOf(GarageTab.BIKES) }

    val selectedIndex = BikeCatalog.bikes.indexOfFirst { it.id == selectedBikeId }.coerceAtLeast(0)
    val baseBike = BikeCatalog.bikes[selectedIndex]
    val selectedPaintId = prefs.bikePaintIds[baseBike.id] ?: BikePaintCatalog.STOCK_ID
    val paintedBike = BikeCatalog.withPaint(baseBike, selectedPaintId)
    val selectedOwned = baseBike.id in prefs.ownedBikeIds
    val selectedEquipped = baseBike.id == prefs.equippedBikeId
    val selectedUpgrades = prefs.bikeUpgrades[baseBike.id] ?: BikeUpgradeState()
    val (tierBadge, rating) = BikeCatalog.tierAndRating(baseBike, selectedUpgrades)

    val goPrevBike = {
        val prev = (selectedIndex - 1 + BikeCatalog.bikes.size) % BikeCatalog.bikes.size
        selectedBikeId = BikeCatalog.bikes[prev].id
    }
    val goNextBike = {
        val next = (selectedIndex + 1) % BikeCatalog.bikes.size
        selectedBikeId = BikeCatalog.bikes[next].id
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1080.dp)
                .padding(horizontal = 16.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                MotorsportSheetHeader(
                    title = "GARAGE & TUNING",
                    subtitle = if (activeTab == GarageTab.BIKES) "CHOOSE YOUR RIDE" else "UPGRADE YOUR MOTORCYCLE",
                    backTag = "garage_back",
                    onBack = onClose,
                    trailingContent = { CurrencyPill(usdBalance = prefs.usdBalance) }
                )

                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left vertical tab rail matching mockup
                        Column(
                            modifier = Modifier
                                .width(114.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalPanel)
                                .border(1.dp, CharcoalHairline, RoundedCornerShape(14.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GarageTab.entries.forEach { tab ->
                                val selected = activeTab == tab
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selected) {
                                                Brush.horizontalGradient(listOf(CrimsonPlayStart, CrimsonPlayEnd))
                                            } else {
                                                Brush.horizontalGradient(listOf(CharcoalElevated, CharcoalElevated))
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selected) Color.White.copy(alpha = 0.25f) else CharcoalHairline,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { activeTab = tab }
                                        .padding(horizontal = 10.dp)
                                        .testTag(tab.tag),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (tab) {
                                            GarageTab.BIKES -> Icons.Default.TwoWheeler
                                            GarageTab.UPGRADES -> Icons.Default.Tune
                                            GarageTab.PAINT -> Icons.Default.Palette
                                        },
                                        contentDescription = null,
                                        tint = if (selected) Color.White else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = tab.label,
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else TextMuted,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CharcoalBg)
                                    .border(1.dp, CharcoalHairline, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${baseBike.displacementCc}CC",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = "+${((baseBike.payoutBonus - 1f) * 100).toInt()}% USD",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                )
                            }
                        }

                        // Center Studio Spotlight Stage + Horizontal Fleet Thumbnail Carousel
                        Column(
                            modifier = Modifier
                                .weight(0.46f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudioSpotlightStage(
                                bike = paintedBike,
                                upgrades = selectedUpgrades,
                                tierBadge = tierBadge,
                                owned = selectedOwned,
                                equipped = selectedEquipped,
                                onPrev = goPrevBike,
                                onNext = goNextBike,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            FleetThumbnailStrip(
                                prefs = prefs,
                                selectedBikeId = baseBike.id,
                                compact = true,
                                onSelectBike = { selectedBikeId = it }
                            )
                        }

                        // Right Spec Telemetry + Dynamic Tab Content (BIKES / UPGRADES / PAINT)
                        Column(
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CharcoalPanel)
                                .border(1.dp, CharcoalHairline, RoundedCornerShape(14.dp))
                                .padding(12.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BikeTitleAndRatingRow(
                                bike = baseBike,
                                rating = rating,
                                compact = true
                            )

                            CompactSpecBarsGrid(
                                bike = baseBike,
                                upgrades = selectedUpgrades
                            )

                            when (activeTab) {
                                GarageTab.BIKES -> {
                                    Text(
                                        text = baseBike.tagline,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    BikeOwnershipActionButton(
                                        bike = baseBike,
                                        owned = selectedOwned,
                                        equipped = selectedEquipped,
                                        usdBalance = prefs.usdBalance,
                                        onBuy = { onBuyBike(baseBike) },
                                        onEquip = { onEquipBike(baseBike.id) }
                                    )
                                    UpgradeSectionCompact(
                                        bike = baseBike,
                                        upgrades = selectedUpgrades,
                                        owned = selectedOwned,
                                        usdBalance = prefs.usdBalance,
                                        compact = true,
                                        onUpgrade = { stat -> onUpgradeBike(baseBike, stat) }
                                    )
                                }
                                GarageTab.UPGRADES -> {
                                    UpgradeSectionCompact(
                                        bike = baseBike,
                                        upgrades = selectedUpgrades,
                                        owned = selectedOwned,
                                        usdBalance = prefs.usdBalance,
                                        compact = true,
                                        onUpgrade = { stat -> onUpgradeBike(baseBike, stat) }
                                    )
                                }
                                GarageTab.PAINT -> {
                                    PaintLiveryPicker(
                                        selectedPaintId = selectedPaintId,
                                        onSelectPaint = { paintId -> onSelectPaint(baseBike.id, paintId) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Portrait Garage layout matching mockup
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CharcoalPanel)
                            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GarageTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (selected) {
                                            Brush.horizontalGradient(listOf(CrimsonPlayStart, CrimsonPlayEnd))
                                        } else {
                                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                        }
                                    )
                                    .clickable { activeTab = tab }
                                    .testTag(tab.tag),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.label,
                                    fontFamily = OrbitronFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) Color.White else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioSpotlightStage(
                            bike = paintedBike,
                            upgrades = selectedUpgrades,
                            tierBadge = tierBadge,
                            owned = selectedOwned,
                            equipped = selectedEquipped,
                            onPrev = goPrevBike,
                            onNext = goNextBike,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CharcoalPanel)
                                .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            BikeTitleAndRatingRow(
                                bike = baseBike,
                                rating = rating,
                                compact = false
                            )

                            CompactSpecBarsGrid(
                                bike = baseBike,
                                upgrades = selectedUpgrades
                            )

                            when (activeTab) {
                                GarageTab.BIKES -> {
                                    BikeOwnershipActionButton(
                                        bike = baseBike,
                                        owned = selectedOwned,
                                        equipped = selectedEquipped,
                                        usdBalance = prefs.usdBalance,
                                        onBuy = { onBuyBike(baseBike) },
                                        onEquip = { onEquipBike(baseBike.id) }
                                    )
                                    FleetThumbnailStrip(
                                        prefs = prefs,
                                        selectedBikeId = baseBike.id,
                                        compact = false,
                                        onSelectBike = { selectedBikeId = it }
                                    )
                                    UpgradeSectionCompact(
                                        bike = baseBike,
                                        upgrades = selectedUpgrades,
                                        owned = selectedOwned,
                                        usdBalance = prefs.usdBalance,
                                        compact = false,
                                        onUpgrade = { stat -> onUpgradeBike(baseBike, stat) }
                                    )
                                }
                                GarageTab.UPGRADES -> {
                                    UpgradeSectionCompact(
                                        bike = baseBike,
                                        upgrades = selectedUpgrades,
                                        owned = selectedOwned,
                                        usdBalance = prefs.usdBalance,
                                        compact = false,
                                        onUpgrade = { stat -> onUpgradeBike(baseBike, stat) }
                                    )
                                    if (!selectedOwned) {
                                        BikeOwnershipActionButton(
                                            bike = baseBike,
                                            owned = false,
                                            equipped = false,
                                            usdBalance = prefs.usdBalance,
                                            onBuy = { onBuyBike(baseBike) },
                                            onEquip = { onEquipBike(baseBike.id) }
                                        )
                                    }
                                }
                                GarageTab.PAINT -> {
                                    PaintLiveryPicker(
                                        selectedPaintId = selectedPaintId,
                                        onSelectPaint = { paintId -> onSelectPaint(baseBike.id, paintId) }
                                    )
                                }
                            }
                        }
                    }

                    PaddockBottomNavBar(
                        currentTab = PaddockNavTab.GARAGE,
                        onNavigate = onNavigateTab
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioSpotlightStage(
    bike: BikeModel,
    upgrades: BikeUpgradeState,
    tierBadge: String,
    owned: Boolean,
    equipped: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel)
            .border(1.2.dp, Color(bike.primaryColor).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_garage_spotlight_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CharcoalBg.copy(alpha = 0.25f),
                            CharcoalBg.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        MotorcycleShowcaseCanvas(
            bike = bike,
            upgrades = upgrades,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 10.dp)
        )

        // Left / Right Carousel arrows (48dp touch targets)
        IconButton(
            onClick = onPrev,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(CharcoalBg.copy(alpha = 0.72f))
                .border(1.dp, CharcoalHairline, CircleShape)
                .testTag("garage_prev_bike")
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Bike", tint = TextPrimary)
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(CharcoalBg.copy(alpha = 0.72f))
                .border(1.dp, CharcoalHairline, CircleShape)
                .testTag("garage_next_bike")
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Bike", tint = TextPrimary)
        }

        // Top-left status & displacement
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            when {
                equipped -> StatusTagPill(text = "SELECTED", accentColor = EmeraldAccent)
                owned -> StatusTagPill(text = "OWNED", accentColor = CyanAccent)
                else -> StatusTagPill(
                    text = "$${String.format(Locale.US, "%,d", bike.msrpUsd)}",
                    accentColor = AmberAccent
                )
            }
            StatusTagPill(text = "TUNE LV.${upgrades.totalUpgradeLevel}/15", accentColor = TextPrimary)
        }

        // Top-right Tier Badge (S / A / B) matching mockup
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AmberAccent)
                .padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Text(
                text = tierBadge,
                fontFamily = OrbitronFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = CharcoalBg
            )
        }
    }
}

@Composable
private fun BikeTitleAndRatingRow(
    bike: BikeModel,
    rating: Float,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${bike.year} ${bike.manufacturer.uppercase(Locale.US)} · ${bike.category}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = bike.modelName,
                fontFamily = OrbitronFontFamily,
                fontSize = if (compact) 16.sp else 19.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("selected_bike_title")
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val fullStars = rating.toInt().coerceIn(3, 5)
            repeat(5) { idx ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (idx < fullStars) AmberAccent else Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = String.format(Locale.US, "%.1f", rating),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AmberAccent
            )
        }
    }
}

@Composable
private fun CompactSpecBarsGrid(
    bike: BikeModel,
    upgrades: BikeUpgradeState
) {
    val speedMult = BikeCatalog.effectiveTopSpeedMult(bike, upgrades)
    val accelMult = BikeCatalog.effectiveAccelMult(bike, upgrades)
    val gripMult = BikeCatalog.effectiveGripMult(bike, upgrades)
    val nitroMult = BikeCatalog.effectiveNitroRechargeMult(bike, upgrades)
    val topSpeedKmh = (GameEngine.MAXSP * speedMult * GameEngine.KMH).toInt()

    val speedNorm = ((speedMult - 0.9f) / 0.58f).coerceIn(0.15f, 1f)
    val accelNorm = ((accelMult - 0.9f) / 0.62f).coerceIn(0.15f, 1f)
    val gripNorm = ((gripMult - 0.9f) / 0.56f).coerceIn(0.15f, 1f)
    val nitroNorm = ((nitroMult - 0.9f) / 0.68f).coerceIn(0.15f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(CharcoalElevated)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaddockSpecStatBar(
                label = "TOP SPEED",
                valueLabel = "$topSpeedKmh KM/H",
                progress = speedNorm,
                accentColor = CrimsonPlayStart,
                gradientColors = listOf(AmberAccent, CrimsonPlayStart),
                modifier = Modifier.weight(1f)
            )
            PaddockSpecStatBar(
                label = "ACCEL",
                valueLabel = "${String.format(Locale.US, "%.2f", accelMult)}×",
                progress = accelNorm,
                accentColor = EmeraldAccent,
                gradientColors = listOf(CyanAccent, EmeraldAccent),
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PaddockSpecStatBar(
                label = "CORNER GRIP",
                valueLabel = "${String.format(Locale.US, "%.2f", gripMult)}×",
                progress = gripNorm,
                accentColor = CyanAccent,
                gradientColors = listOf(ElectricBlueStart, ElectricBlueEnd),
                modifier = Modifier.weight(1f)
            )
            PaddockSpecStatBar(
                label = "NITRO",
                valueLabel = "${String.format(Locale.US, "%.2f", nitroMult)}×",
                progress = nitroNorm,
                accentColor = MagentaAccent,
                gradientColors = listOf(CyanAccent, MagentaAccent),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BikeOwnershipActionButton(
    bike: BikeModel,
    owned: Boolean,
    equipped: Boolean,
    usdBalance: Int,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    when {
        equipped -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(SelectedGreenStart, SelectedGreenEnd)))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = CharcoalBg, modifier = Modifier.size(18.dp))
                    Text(
                        text = "SELECTED ✓",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CharcoalBg
                    )
                }
            }
        }
        owned -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(ElectricBlueStart, ElectricBlueEnd)))
                    .clickable(onClick = onEquip)
                    .testTag("equip_${bike.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EQUIP MOTORCYCLE",
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = CharcoalBg
                )
            }
        }
        else -> {
            val canAfford = usdBalance >= bike.msrpUsd
            Button(
                onClick = onBuy,
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) EmeraldAccent else CharcoalElevated,
                    contentColor = if (canAfford) CharcoalBg else TextMuted
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("buy_${bike.id}")
            ) {
                Text(
                    text = if (canAfford) {
                        "BUY FOR $${String.format(Locale.US, "%,d", bike.msrpUsd)}"
                    } else {
                        "NEED $${String.format(Locale.US, "%,d", bike.msrpUsd - usdBalance)} MORE USD"
                    },
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun FleetThumbnailStrip(
    prefs: PlayerPreferencesSnapshot,
    selectedBikeId: String,
    compact: Boolean,
    onSelectBike: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(BikeCatalog.bikes, key = { it.id }) { bike ->
            val owned = bike.id in prefs.ownedBikeIds
            val equipped = bike.id == prefs.equippedBikeId
            val inspected = bike.id == selectedBikeId
            val painted = BikeCatalog.withPaint(bike, prefs.bikePaintIds[bike.id])
            val upgrades = prefs.bikeUpgrades[bike.id] ?: BikeUpgradeState()

            Column(
                modifier = Modifier
                    .width(if (compact) 104.dp else 112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (inspected) CharcoalElevated else CharcoalPanel)
                    .border(
                        width = if (inspected) 1.6.dp else 1.dp,
                        color = when {
                            inspected -> EmeraldAccent
                            equipped -> CyanAccent
                            else -> CharcoalHairline
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectBike(bike.id) }
                    .padding(6.dp)
                    .testTag("bike_card_${bike.id}"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 42.dp else 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalBg)
                ) {
                    MotorcycleShowcaseCanvas(
                        bike = painted,
                        upgrades = upgrades,
                        showPaddockStage = false,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (!owned) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = TextMuted,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(3.dp)
                                .size(12.dp)
                        )
                    }
                }
                Text(
                    text = bike.modelName,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (inspected) TextPrimary else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun UpgradeSectionCompact(
    bike: BikeModel,
    upgrades: BikeUpgradeState,
    owned: Boolean,
    usdBalance: Int,
    compact: Boolean,
    onUpgrade: (UpgradeStat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp)) {
        UpgradeModuleCard(
            stat = UpgradeStat.ENGINE,
            effectSummary = "+4% Top Speed · +6% Accel / lv",
            icon = Icons.Default.Speed,
            level = upgrades.engineLevel,
            cost = BikeCatalog.upgradeCostUsd(bike, upgrades.engineLevel),
            owned = owned,
            usdBalance = usdBalance,
            accentColor = CrimsonPlayStart,
            compact = compact,
            onClick = { onUpgrade(UpgradeStat.ENGINE) }
        )
        UpgradeModuleCard(
            stat = UpgradeStat.TIRES,
            effectSummary = "+5.5% Cornering Stability / lv",
            icon = Icons.Default.TireRepair,
            level = upgrades.tiresLevel,
            cost = BikeCatalog.upgradeCostUsd(bike, upgrades.tiresLevel),
            owned = owned,
            usdBalance = usdBalance,
            accentColor = EmeraldAccent,
            compact = compact,
            onClick = { onUpgrade(UpgradeStat.TIRES) }
        )
        UpgradeModuleCard(
            stat = UpgradeStat.NITRO,
            effectSummary = "+7% Recharge · -8% Burn / lv",
            icon = Icons.Default.Bolt,
            level = upgrades.nitroLevel,
            cost = BikeCatalog.upgradeCostUsd(bike, upgrades.nitroLevel),
            owned = owned,
            usdBalance = usdBalance,
            accentColor = CyanAccent,
            compact = compact,
            onClick = { onUpgrade(UpgradeStat.NITRO) }
        )
    }
}

@Composable
private fun UpgradeModuleCard(
    stat: UpgradeStat,
    effectSummary: String,
    icon: ImageVector,
    level: Int,
    cost: Int,
    owned: Boolean,
    usdBalance: Int,
    accentColor: Color,
    compact: Boolean,
    onClick: () -> Unit
) {
    val isMax = level >= BikeCatalog.MAX_UPGRADE_LEVEL
    val canAfford = owned && !isMax && usdBalance >= cost

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(17.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stat.label,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "LV.$level/${BikeCatalog.MAX_UPGRADE_LEVEL}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMax) CyanAccent else AmberAccent
                    )
                }
                Text(
                    text = effectSummary,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (step in 1..BikeCatalog.MAX_UPGRADE_LEVEL) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (step <= level) accentColor else Color.White.copy(alpha = 0.10f)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        when {
            !owned -> {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CharcoalBg)
                        .border(1.dp, CharcoalHairline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
            isMax -> {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyanAccent.copy(alpha = 0.12f))
                        .border(1.dp, CyanAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MAX", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = CyanAccent)
                }
            }
            else -> {
                Button(
                    onClick = onClick,
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAfford) CrimsonPlayStart else CharcoalBg,
                        contentColor = if (canAfford) Color.White else TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("upgrade_${stat.name}")
                ) {
                    Text(
                        text = "$${String.format(Locale.US, "%,d", cost)}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PaintLiveryPicker(
    selectedPaintId: String,
    onSelectPaint: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalElevated)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CUSTOM FAIRING PAINT (FREE LIVERY)",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyanAccent
        )

        val rows = remember { BikePaintCatalog.swatches.chunked(3) }
        rows.forEach { rowSwatches ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSwatches.forEach { swatch ->
                    val isSelected = swatch.id == selectedPaintId
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(swatch.primaryColor).copy(alpha = 0.22f) else CharcoalPanel)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(swatch.primaryColor) else CharcoalHairline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectPaint(swatch.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("paint_${swatch.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(swatch.primaryColor), Color(swatch.trimColor))
                                    )
                                )
                                .border(1.5.dp, Color(swatch.accentColor), CircleShape)
                        )
                        Text(
                            text = swatch.label,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) TextPrimary else TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
