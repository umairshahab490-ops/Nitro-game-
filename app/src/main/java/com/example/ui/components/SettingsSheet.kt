package com.example.ui.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerPreferencesSnapshot
import com.example.model.Difficulty
import com.example.model.GameMode
import com.example.model.TrafficMode
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalHairline
import com.example.ui.theme.CharcoalPanel
import com.example.ui.theme.CrimsonPlayEnd
import com.example.ui.theme.CrimsonPlayStart
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MagentaAccent
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SettingsSheet(
    prefs: PlayerPreferencesSnapshot,
    tiltAvailable: Boolean,
    onSelectDifficulty: (Difficulty) -> Unit,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectTrafficMode: (TrafficMode) -> Unit,
    onToggleTilt: () -> Unit,
    onToggleMute: () -> Unit,
    onNavigateTab: (PaddockNavTab) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg.copy(alpha = 0.97f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1040.dp)
                .padding(horizontal = 16.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                MotorsportSheetHeader(
                    title = "RACE SETTINGS",
                    subtitle = "DIFFICULTY · GAME MODE · TRAFFIC · CONTROLS",
                    backTag = "settings_back",
                    onBack = onClose,
                    trailingContent = { CurrencyPill(usdBalance = prefs.usdBalance) }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsOptionGroup(
                                    icon = Icons.Default.Speed,
                                    title = "DIFFICULTY TIER",
                                    subtitle = "Affects traffic speed, grip & USD multiplier",
                                    accentColor = CyanAccent
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Difficulty.entries.forEach { d ->
                                            SettingsSegmentChip(
                                                label = d.label,
                                                selected = prefs.difficulty == d,
                                                accentColor = CyanAccent,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("diff_${d.name}"),
                                                onClick = { onSelectDifficulty(d) }
                                            )
                                        }
                                    }
                                }

                                SettingsOptionGroup(
                                    icon = Icons.Default.Tune,
                                    title = "RACE FORMAT",
                                    subtitle = "Time Attack checkpoints or 3-life Endless survival",
                                    accentColor = MagentaAccent
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        GameMode.entries.forEach { gm ->
                                            SettingsSegmentChip(
                                                label = gm.label,
                                                selected = prefs.gameMode == gm,
                                                accentColor = MagentaAccent,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("mode_${gm.name}"),
                                                onClick = { onSelectGameMode(gm) }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SettingsOptionGroup(
                                    icon = Icons.Default.Traffic,
                                    title = "TRAFFIC FLOW",
                                    subtitle = "Two-Way oncoming traffic pays 2× USD rewards",
                                    accentColor = AmberAccent
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TrafficMode.entries.forEach { tm ->
                                            SettingsSegmentChip(
                                                label = tm.label,
                                                selected = prefs.trafficMode == tm,
                                                accentColor = AmberAccent,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 48.dp)
                                                    .testTag("traffic_${tm.name}"),
                                                onClick = { onSelectTrafficMode(tm) }
                                            )
                                        }
                                    }
                                }

                                SettingsTogglesCard(
                                    prefs = prefs,
                                    tiltAvailable = tiltAvailable,
                                    onToggleTilt = onToggleTilt,
                                    onToggleMute = onToggleMute
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.horizontalGradient(listOf(CrimsonPlayStart, CrimsonPlayEnd)))
                                        .clickable(onClick = onClose)
                                        .testTag("settings_done_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "SAVE & RETURN",
                                        fontFamily = OrbitronFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        SettingsOptionGroup(
                            icon = Icons.Default.Speed,
                            title = "DIFFICULTY TIER",
                            subtitle = "Affects traffic density, road grip & USD multiplier",
                            accentColor = CyanAccent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Difficulty.entries.forEach { d ->
                                    SettingsSegmentChip(
                                        label = d.label,
                                        selected = prefs.difficulty == d,
                                        accentColor = CyanAccent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("diff_${d.name}"),
                                        onClick = { onSelectDifficulty(d) }
                                    )
                                }
                            }
                        }

                        SettingsOptionGroup(
                            icon = Icons.Default.Tune,
                            title = "RACE FORMAT",
                            subtitle = "Time Attack checkpoints or 3-life Endless survival",
                            accentColor = MagentaAccent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GameMode.entries.forEach { gm ->
                                    SettingsSegmentChip(
                                        label = gm.label,
                                        selected = prefs.gameMode == gm,
                                        accentColor = MagentaAccent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("mode_${gm.name}"),
                                        onClick = { onSelectGameMode(gm) }
                                    )
                                }
                            }
                        }

                        SettingsOptionGroup(
                            icon = Icons.Default.Traffic,
                            title = "TRAFFIC FLOW",
                            subtitle = "Two-Way oncoming traffic pays 2× USD rewards",
                            accentColor = AmberAccent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TrafficMode.entries.forEach { tm ->
                                    SettingsSegmentChip(
                                        label = tm.label,
                                        selected = prefs.trafficMode == tm,
                                        accentColor = AmberAccent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("traffic_${tm.name}"),
                                        onClick = { onSelectTrafficMode(tm) }
                                    )
                                }
                            }
                        }

                        SettingsTogglesCard(
                            prefs = prefs,
                            tiltAvailable = tiltAvailable,
                            onToggleTilt = onToggleTilt,
                            onToggleMute = onToggleMute
                        )

                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPlayStart,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("settings_done_button")
                        ) {
                            Text(
                                text = "APPLY & BACK",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                if (!isLandscape) {
                    PaddockBottomNavBar(
                        currentTab = PaddockNavTab.SETTINGS,
                        onNavigate = onNavigateTab
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionGroup(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
        content()
    }
}

@Composable
private fun SettingsSegmentChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentColor.copy(alpha = 0.18f) else CharcoalElevated)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) accentColor else CharcoalHairline,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) accentColor else TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsTogglesCard(
    prefs: PlayerPreferencesSnapshot,
    tiltAvailable: Boolean,
    onToggleTilt: () -> Unit,
    onToggleMute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (tiltAvailable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalElevated)
                    .clickable(onClick = onToggleTilt)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("tilt_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = if (prefs.tiltEnabled) EmeraldAccent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TILT STEERING",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Lean device to steer alongside touch pads",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                Switch(
                    checked = prefs.tiltEnabled,
                    onCheckedChange = { onToggleTilt() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CharcoalBg,
                        checkedTrackColor = EmeraldAccent
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CharcoalElevated)
                .clickable(onClick = onToggleMute)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("settings_mute_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (prefs.muted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (prefs.muted) TextMuted else CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "ENGINE & RACE AUDIO",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (prefs.muted) "Audio muted" else "Synthesized 6-gear exhaust & effects active",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
            Switch(
                checked = !prefs.muted,
                onCheckedChange = { onToggleMute() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CharcoalBg,
                    checkedTrackColor = CyanAccent
                )
            )
        }
    }
}
