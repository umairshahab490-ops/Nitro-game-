package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.BikeCatalog
import com.example.model.RealRoadTrack
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CrimsonPlayEnd
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.GoldPlayEnd
import com.example.ui.theme.GoldPlayStart
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.util.Locale

@Composable
fun MainMenuOverlay(
    prefs: PlayerPreferencesSnapshot,
    activeRoute: RealRoadTrack,
    claimableMissionCount: Int,
    onOpenGarage: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenGpsMap: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartRide: () -> Unit
) {
    val equippedBike = BikeCatalog.findById(prefs.equippedBikeId)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg)
    ) {
        val isLandscape = maxWidth > maxHeight
        val isCompactLandscape = isLandscape && maxHeight < 390.dp

        // Full-bleed key art backdrop
        Image(
            painter = painterResource(id = R.drawable.img_menu_hero_keyart),
            contentDescription = "Nitro Rush Superbike Highway Key Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Contrast scrims tuned to preserve rider centerpiece while keeping controls crisp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isLandscape) {
                        Brush.horizontalGradient(
                            0.0f to CharcoalBg.copy(alpha = 0.28f),
                            0.46f to CharcoalBg.copy(alpha = 0.45f),
                            1.0f to CharcoalBg.copy(alpha = 0.88f)
                        )
                    } else {
                        Brush.verticalGradient(
                            0.0f to CharcoalBg.copy(alpha = 0.78f),
                            0.28f to CharcoalBg.copy(alpha = 0.16f),
                            0.55f to CharcoalBg.copy(alpha = 0.45f),
                            1.0f to CharcoalBg.copy(alpha = 0.94f)
                        )
                    }
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = if (isLandscape) 18.dp else 16.dp, vertical = if (isCompactLandscape) 8.dp else 12.dp)
        ) {
            if (isLandscape) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top bar: Settings Gear left, Currency + Mute clearance right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TopSettingsGearButton(
                            tag = "open_settings_gear",
                            onClick = onOpenSettings
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BestScoreChip(bestScore = prefs.bestScore)
                            CurrencyPill(usdBalance = prefs.usdBalance)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = if (isCompactLandscape) 2.dp else 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Left hero identity column: Logo at top-left, Legend + active setup strip at bottom-left
                        Column(
                            modifier = Modifier
                                .weight(0.50f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            NitroRushEmblemLogo(
                                compact = true,
                                modifier = Modifier.padding(start = 6.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                ActiveCourseQuickPill(
                                    bikeName = equippedBike.fullName,
                                    routeName = activeRoute.roadName,
                                    difficultyLabel = prefs.difficulty.label,
                                    onClick = onOpenGpsMap
                                )
                                LegendPromoBanner(
                                    compact = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Right Action Deck: Gold PLAY NOW + 2x2 Paddock Grid (GARAGE/MISSIONS/MAP/SETTINGS)
                        Column(
                            modifier = Modifier
                                .weight(0.50f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 370.dp),
                                verticalArrangement = Arrangement.spacedBy(if (isCompactLandscape) 7.dp else 9.dp)
                            ) {
                                GradientPlayButton(
                                    gradient = listOf(GoldPlayStart, GoldPlayEnd),
                                    contentColor = CharcoalBg,
                                    compact = isCompactLandscape,
                                    onClick = onStartRide
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MenuGridButton(
                                        label = "GARAGE",
                                        icon = Icons.Default.TwoWheeler,
                                        accent = TextPrimary,
                                        compact = isCompactLandscape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_garage"),
                                        onClick = onOpenGarage
                                    )
                                    MenuGridButton(
                                        label = "MISSIONS",
                                        icon = Icons.Default.EmojiEvents,
                                        accent = AmberAccent,
                                        badgeCount = claimableMissionCount,
                                        compact = isCompactLandscape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_missions"),
                                        onClick = onOpenMissions
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MenuGridButton(
                                        label = "MAP",
                                        icon = Icons.Default.LocationOn,
                                        accent = CyanAccent,
                                        compact = isCompactLandscape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_map"),
                                        onClick = onOpenGpsMap
                                    )
                                    MenuGridButton(
                                        label = "SETTINGS",
                                        icon = Icons.Default.Settings,
                                        accent = TextMuted,
                                        compact = isCompactLandscape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("open_settings"),
                                        onClick = onOpenSettings
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait Main Menu matching mockup: Settings top-left, Currency top-right (clearing mute),
                // Nitro Rush emblem centered upper sky, open hero rider middle, Crimson PLAY NOW + 2x2 grid + Legend banner bottom
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 480.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 56.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TopSettingsGearButton(
                            tag = "open_settings_gear",
                            onClick = onOpenSettings
                        )
                        CurrencyPill(usdBalance = prefs.usdBalance)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    NitroRushEmblemLogo(compact = false)

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ActiveCourseQuickPill(
                            bikeName = equippedBike.fullName,
                            routeName = activeRoute.roadName,
                            difficultyLabel = prefs.difficulty.label,
                            onClick = onOpenGpsMap
                        )

                        GradientPlayButton(
                            gradient = listOf(CrimsonPlayStart, CrimsonPlayEnd),
                            contentColor = Color.White,
                            compact = false,
                            onClick = onStartRide
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MenuGridButton(
                                label = "GARAGE",
                                icon = Icons.Default.TwoWheeler,
                                accent = TextPrimary,
                                compact = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_garage"),
                                onClick = onOpenGarage
                            )
                            MenuGridButton(
                                label = "MISSIONS",
                                icon = Icons.Default.EmojiEvents,
                                accent = AmberAccent,
                                badgeCount = claimableMissionCount,
                                compact = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_missions"),
                                onClick = onOpenMissions
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MenuGridButton(
                                label = "MAP",
                                icon = Icons.Default.LocationOn,
                                accent = CyanAccent,
                                compact = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_map"),
                                onClick = onOpenGpsMap
                            )
                            MenuGridButton(
                                label = "SETTINGS",
                                icon = Icons.Default.Settings,
                                accent = TextMuted,
                                compact = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_settings"),
                                onClick = onOpenSettings
                            )
                        }

                        LegendPromoBanner(
                            compact = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSettingsGearButton(
    tag: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalPanel.copy(alpha = 0.85f))
            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
            .testTag(tag)
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = TextPrimary
        )
    }
}

@Composable
private fun BestScoreChip(bestScore: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalPanel.copy(alpha = 0.88f))
            .border(1.dp, CharcoalHairline, RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "BEST ${String.format(Locale.US, "%,d", bestScore)}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun ActiveCourseQuickPill(
    bikeName: String,
    routeName: String,
    difficultyLabel: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalPanel.copy(alpha = 0.86f))
            .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Route,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "$bikeName · $routeName",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        StatusTagPill(text = difficultyLabel, accentColor = AmberAccent)
    }
}

@Composable
private fun GradientPlayButton(
    gradient: List<Color>,
    contentColor: Color,
    compact: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 48.dp else 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(gradient))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("start_ride_button"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (compact) 22.dp else 26.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "PLAY NOW",
                fontFamily = OrbitronFontFamily,
                fontSize = if (compact) 17.sp else 20.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 1.6.sp,
                color = contentColor
            )
        }
    }
}

@Composable
private fun MenuGridButton(
    label: String,
    icon: ImageVector,
    accent: Color,
    compact: Boolean,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(if (compact) 48.dp else 50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalPanel.copy(alpha = 0.90f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(if (compact) 17.dp else 19.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                fontFamily = OrbitronFontFamily,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MagentaAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LegendPromoBanner(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        CharcoalPanel.copy(alpha = 0.92f),
                        AmberAccent.copy(alpha = 0.22f),
                        CharcoalPanel.copy(alpha = 0.92f)
                    )
                )
            )
            .border(1.dp, AmberAccent.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = if (compact) 6.dp else 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = "BECOME A LEGEND!",
            fontFamily = OrbitronFontFamily,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 1.2.sp,
            color = AmberAccent
        )
    }
}
