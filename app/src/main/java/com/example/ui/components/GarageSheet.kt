package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerPreferencesSnapshot
import com.example.model.BikeCatalog
import com.example.model.BikeModel
import com.example.model.BikeUpgradeState
import com.example.model.UpgradeStat
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BoostCyan
import com.example.ui.theme.CashGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun GarageSheet(
    prefs: PlayerPreferencesSnapshot,
    onBuyBike: (BikeModel) -> Unit,
    onEquipBike: (String) -> Unit,
    onUpgradeBike: (BikeModel, UpgradeStat) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.96f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("garage_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "GARAGE & TUNING",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "REAL MOTORCYCLES · ACTUAL 2025 US MSRP",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CashGreen.copy(alpha = 0.14f))
                        .border(1.dp, CashGreen.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = CashGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = String.format(Locale.US, "%,d USD", prefs.usdBalance),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CashGreen
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(BikeCatalog.bikes, key = { it.id }) { bike ->
                    val owned = bike.id in prefs.ownedBikeIds
                    val equipped = bike.id == prefs.equippedBikeId
                    val upgrades = prefs.bikeUpgrades[bike.id] ?: BikeUpgradeState()
                    val canAfford = prefs.usdBalance >= bike.msrpUsd

                    BikeShowroomCard(
                        bike = bike,
                        upgrades = upgrades,
                        owned = owned,
                        equipped = equipped,
                        canAfford = canAfford,
                        usdBalance = prefs.usdBalance,
                        onBuy = { onBuyBike(bike) },
                        onEquip = { onEquipBike(bike.id) },
                        onUpgrade = { stat -> onUpgradeBike(bike, stat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BikeShowroomCard(
    bike: BikeModel,
    upgrades: BikeUpgradeState,
    owned: Boolean,
    equipped: Boolean,
    canAfford: Boolean,
    usdBalance: Int,
    onBuy: () -> Unit,
    onEquip: () -> Unit,
    onUpgrade: (UpgradeStat) -> Unit
) {
    val borderColor = when {
        equipped -> BoostCyan
        owned -> Color.White.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (equipped) SurfaceElevated else SurfaceCard)
            .border(if (equipped) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                BikePreviewBadge(bike = bike)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${bike.year} ${bike.manufacturer.uppercase()} · ${bike.displacementCc}CC",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BoostCyan
                    )
                    Text(
                        text = bike.modelName,
                        fontFamily = OrbitronFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = bike.tagline,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%,d", bike.msrpUsd)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CashGreen
                )
                Text(
                    text = "USD MSRP · +${((bike.payoutBonus - 1f) * 100).toInt()}% PAYOUT",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    color = AmberGold
                )
            }
        }

        // Performance Bars
        val speedNorm = ((BikeCatalog.effectiveTopSpeedMult(bike, upgrades) - 0.9f) / 0.58f).coerceIn(0.15f, 1f)
        val accelNorm = ((BikeCatalog.effectiveAccelMult(bike, upgrades) - 0.9f) / 0.62f).coerceIn(0.15f, 1f)
        val gripNorm = ((BikeCatalog.effectiveGripMult(bike, upgrades) - 0.9f) / 0.56f).coerceIn(0.15f, 1f)
        val nitroNorm = ((BikeCatalog.effectiveNitroRechargeMult(bike, upgrades) - 0.9f) / 0.68f).coerceIn(0.15f, 1f)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatMeter("TOP SPEED", speedNorm, NeonPink)
            StatMeter("ACCEL", accelNorm, AmberGold)
            StatMeter("CORNER GRIP", gripNorm, CashGreen)
            StatMeter("NITRO", nitroNorm, BoostCyan)
        }

        if (owned) {
            // Engine / Tires / Nitro Upgrades
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UpgradeButton(
                    stat = UpgradeStat.ENGINE,
                    level = upgrades.engineLevel,
                    cost = BikeCatalog.upgradeCostUsd(bike, upgrades.engineLevel),
                    canAfford = usdBalance >= BikeCatalog.upgradeCostUsd(bike, upgrades.engineLevel),
                    modifier = Modifier.weight(1f),
                    onClick = { onUpgrade(UpgradeStat.ENGINE) }
                )
                UpgradeButton(
                    stat = UpgradeStat.TIRES,
                    level = upgrades.tiresLevel,
                    cost = BikeCatalog.upgradeCostUsd(bike, upgrades.tiresLevel),
                    canAfford = usdBalance >= BikeCatalog.upgradeCostUsd(bike, upgrades.tiresLevel),
                    modifier = Modifier.weight(1f),
                    onClick = { onUpgrade(UpgradeStat.TIRES) }
                )
                UpgradeButton(
                    stat = UpgradeStat.NITRO,
                    level = upgrades.nitroLevel,
                    cost = BikeCatalog.upgradeCostUsd(bike, upgrades.nitroLevel),
                    canAfford = usdBalance >= BikeCatalog.upgradeCostUsd(bike, upgrades.nitroLevel),
                    modifier = Modifier.weight(1f),
                    onClick = { onUpgrade(UpgradeStat.NITRO) }
                )
            }

            if (equipped) {
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BoostCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EQUIPPED FOR RACE", fontFamily = JetBrainsMonoFontFamily, color = BoostCyan, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Button(
                    onClick = onEquip,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("equip_${bike.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = BoostCyan, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EQUIP BIKE", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("buy_${bike.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) CashGreen else SurfaceElevated,
                    contentColor = if (canAfford) ObsidianBg else TextMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (canAfford) {
                        "BUY FOR $${String.format(Locale.US, "%,d", bike.msrpUsd)} USD"
                    } else {
                        "NEED $${String.format(Locale.US, "%,d", bike.msrpUsd - usdBalance)} MORE USD"
                    },
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun UpgradeButton(
    stat: UpgradeStat,
    level: Int,
    cost: Int,
    canAfford: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isMax = level >= BikeCatalog.MAX_UPGRADE_LEVEL
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBg.copy(alpha = 0.65f))
            .border(
                1.dp,
                if (isMax) BoostCyan.copy(alpha = 0.45f) else if (canAfford) CashGreen.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isMax && canAfford, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${stat.label} LV.$level",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = if (isMax) "MAXED" else "+UPG $${String.format(Locale.US, "%,d", cost)}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                isMax -> BoostCyan
                canAfford -> CashGreen
                else -> TextMuted
            }
        )
    }
}

@Composable
private fun StatMeter(label: String, progress: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = TextMuted,
            modifier = Modifier.width(86.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
private fun BikePreviewBadge(bike: BikeModel) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianBg)
            .border(1.dp, Color(bike.primaryColor).copy(alpha = 0.55f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val w = size.width
            val h = size.height
            // Rear wheel
            drawRoundRect(
                Color(0xFF10131B),
                Offset(w * 0.38f, h * 0.60f),
                Size(w * 0.24f, h * 0.34f),
                CornerRadius(w * 0.08f)
            )
            // Fairing in real bike livery
            val body = Path().apply {
                moveTo(w * 0.20f, h * 0.62f)
                lineTo(w * 0.80f, h * 0.62f)
                lineTo(w * 0.68f, h * 0.34f)
                lineTo(w * 0.32f, h * 0.34f)
                close()
            }
            drawPath(body, Color(bike.primaryColor))
            drawRoundRect(Color(bike.accentColor), Offset(w * 0.36f, h * 0.14f), Size(w * 0.28f, h * 0.14f), CornerRadius(4f))
            drawCircle(Color(bike.trimColor), radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.5f))
        }
    }
}
