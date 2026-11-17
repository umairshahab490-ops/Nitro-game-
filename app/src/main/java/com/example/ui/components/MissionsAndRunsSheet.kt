package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RunRecordEntity
import com.example.model.CareerMilestone
import com.example.model.DailyMission
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
import java.util.Locale

@Composable
fun MissionsAndRunsSheet(
    dailyMissions: List<DailyMission>,
    milestones: List<CareerMilestone>,
    topRuns: List<RunRecordEntity>,
    usdBalance: Int = 0,
    onClaimDailyMission: (DailyMission) -> Unit,
    onClaimMilestone: (CareerMilestone) -> Unit,
    onNavigateTab: (PaddockNavTab) -> Unit = {},
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var tab by remember { mutableIntStateOf(0) }

    val readyDailyCount = dailyMissions.count { it.isComplete && !it.claimed }
    val readyMilestoneCount = milestones.count { it.isComplete && !it.claimed }
    val readyCount = readyDailyCount + readyMilestoneCount
    val claimedCount = dailyMissions.count { it.claimed } + milestones.count { it.claimed }
    val totalCount = dailyMissions.size + milestones.size

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
                .widthIn(max = 1040.dp)
                .padding(horizontal = 16.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(modifier = Modifier.fillMaxSize()) {
                MotorsportSheetHeader(
                    title = "MISSIONS & BEST RUNS",
                    subtitle = if (readyCount > 0) "$readyCount READY TO CLAIM · $claimedCount/$totalCount COMPLETED"
                    else "$claimedCount/$totalCount COMPLETED · TELEMETRY LOG",
                    backTag = "missions_back",
                    onBack = onClose,
                    trailingContent = { CurrencyPill(usdBalance = usdBalance) }
                )

                PaddockTabSwitcher(
                    selectedTab = tab,
                    readyCount = readyCount,
                    runCount = topRuns.size,
                    onSelectTab = { tab = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (tab == 0) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(0.66f)
                                    .fillMaxHeight(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                item {
                                    MissionSectionHeader(
                                        icon = Icons.Default.Flag,
                                        title = "DAILY MISSIONS",
                                        countLabel = "${dailyMissions.count { it.claimed }}/${dailyMissions.size}",
                                        accentColor = AmberAccent
                                    )
                                }
                                items(dailyMissions, key = { it.id }) { m ->
                                    MissionCard(
                                        tagId = "daily_${m.id}",
                                        title = m.title,
                                        description = m.description,
                                        icon = Icons.Default.EmojiEvents,
                                        progress = m.progress,
                                        target = m.target,
                                        rewardUsd = m.rewardUsd,
                                        claimed = m.claimed,
                                        accentColor = AmberAccent,
                                        highlighted = true,
                                        onClaim = { onClaimDailyMission(m) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    MissionSectionHeader(
                                        icon = Icons.Default.EmojiEvents,
                                        title = "CAREER MILESTONES",
                                        countLabel = "${milestones.count { it.claimed }}/${milestones.size}",
                                        accentColor = CyanAccent
                                    )
                                }
                                items(milestones, key = { it.id }) { ms ->
                                    val iconAndAccent = milestoneIconAndAccent(ms.id)
                                    MissionCard(
                                        tagId = "milestone_${ms.id}",
                                        title = ms.title,
                                        description = ms.description,
                                        icon = iconAndAccent.first,
                                        progress = ms.progress,
                                        target = ms.target,
                                        rewardUsd = ms.rewardUsd,
                                        claimed = ms.claimed,
                                        accentColor = iconAndAccent.second,
                                        highlighted = false,
                                        onClaim = { onClaimMilestone(ms) }
                                    )
                                }
                            }

                            CareerProgressPanel(
                                completedCount = claimedCount,
                                totalCount = totalCount,
                                readyCount = readyCount,
                                modifier = Modifier
                                    .weight(0.34f)
                                    .fillMaxHeight()
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                MissionSectionHeader(
                                    icon = Icons.Default.Flag,
                                    title = "DAILY MISSIONS",
                                    countLabel = "${dailyMissions.count { it.claimed }}/${dailyMissions.size}",
                                    accentColor = AmberAccent
                                )
                            }
                            items(dailyMissions, key = { it.id }) { m ->
                                MissionCard(
                                    tagId = "daily_${m.id}",
                                    title = m.title,
                                    description = m.description,
                                    icon = Icons.Default.EmojiEvents,
                                    progress = m.progress,
                                    target = m.target,
                                    rewardUsd = m.rewardUsd,
                                    claimed = m.claimed,
                                    accentColor = AmberAccent,
                                    highlighted = true,
                                    onClaim = { onClaimDailyMission(m) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                MissionSectionHeader(
                                    icon = Icons.Default.EmojiEvents,
                                    title = "CAREER MILESTONES",
                                    countLabel = "${milestones.count { it.claimed }}/${milestones.size}",
                                    accentColor = CyanAccent
                                )
                            }
                            items(milestones, key = { it.id }) { ms ->
                                val iconAndAccent = milestoneIconAndAccent(ms.id)
                                MissionCard(
                                    tagId = "milestone_${ms.id}",
                                    title = ms.title,
                                    description = ms.description,
                                    icon = iconAndAccent.first,
                                    progress = ms.progress,
                                    target = ms.target,
                                    rewardUsd = ms.rewardUsd,
                                    claimed = ms.claimed,
                                    accentColor = iconAndAccent.second,
                                    highlighted = false,
                                    onClaim = { onClaimMilestone(ms) }
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (topRuns.isEmpty()) {
                            EmptyRunsPlaceholder()
                        } else if (isLandscape) {
                            val rows = remember(topRuns) { topRuns.chunked(2) }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(rows) { rowIndex, pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        RunHistoryCard(
                                            rank = rowIndex * 2 + 1,
                                            run = pair[0],
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (pair.size > 1) {
                                            RunHistoryCard(
                                                rank = rowIndex * 2 + 2,
                                                run = pair[1],
                                                modifier = Modifier.weight(1f)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(topRuns, key = { _, r -> r.id }) { index, run ->
                                    RunHistoryCard(
                                        rank = index + 1,
                                        run = run,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                if (!isLandscape) {
                    PaddockBottomNavBar(
                        currentTab = PaddockNavTab.MISSIONS,
                        onNavigate = onNavigateTab
                    )
                }
            }
        }
    }
}

private fun milestoneIconAndAccent(milestoneId: String): Pair<ImageVector, Color> = when {
    milestoneId.contains("cash") || milestoneId.contains("hustler") -> Icons.Default.AttachMoney to CrimsonPlayStart
    milestoneId.contains("near") || milestoneId.contains("miss") -> Icons.Default.Warning to CyanAccent
    milestoneId.contains("dist") || milestoneId.contains("runner") -> Icons.AutoMirrored.Filled.DirectionsBike to AmberAccent
    milestoneId.contains("bike") || milestoneId.contains("garage") -> Icons.Default.TwoWheeler to EmeraldAccent
    else -> Icons.Default.Speed to CyanAccent
}

@Composable
private fun CareerProgressPanel(
    completedCount: Int,
    totalCount: Int,
    readyCount: Int,
    modifier: Modifier = Modifier
) {
    val safeTotal = totalCount.coerceAtLeast(1)
    val frac = (completedCount.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
    val pct = (frac * 100f).toInt()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CharcoalPanel)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("career_progress_panel"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Career Progress",
            fontFamily = PlusJakartaFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier.size(124.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    brush = Brush.sweepGradient(listOf(EmeraldAccent, CyanAccent, EmeraldAccent)),
                    startAngle = -90f,
                    sweepAngle = (frac * 360f).coerceAtLeast(10f),
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$pct%",
                fontFamily = OrbitronFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "$completedCount / $totalCount",
            fontFamily = OrbitronFontFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Missions",
            fontFamily = PlusJakartaFontFamily,
            fontSize = 12.sp,
            color = TextMuted
        )

        if (readyCount > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            StatusTagPill(
                text = "$readyCount READY TO CLAIM",
                accentColor = EmeraldAccent
            )
        }
    }
}

@Composable
private fun PaddockTabSwitcher(
    selectedTab: Int,
    readyCount: Int,
    runCount: Int,
    onSelectTab: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalPanel)
            .border(1.dp, CharcoalHairline, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TabSegmentButton(
            label = if (readyCount > 0) "MISSIONS ($readyCount)" else "MISSIONS",
            selected = selectedTab == 0,
            useCrimsonGradient = true,
            accentColor = CrimsonPlayStart,
            modifier = Modifier.weight(1f).testTag("tab_missions"),
            onClick = { onSelectTab(0) }
        )
        TabSegmentButton(
            label = if (runCount > 0) "BEST RUNS ($runCount)" else "BEST RUNS",
            selected = selectedTab == 1,
            useCrimsonGradient = false,
            accentColor = AmberAccent,
            modifier = Modifier.weight(1f).testTag("tab_best_runs"),
            onClick = { onSelectTab(1) }
        )
    }
}

@Composable
private fun TabSegmentButton(
    label: String,
    selected: Boolean,
    useCrimsonGradient: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgBrush = when {
        selected && useCrimsonGradient -> Brush.horizontalGradient(listOf(CrimsonPlayStart, CrimsonPlayEnd))
        selected -> Brush.horizontalGradient(listOf(CharcoalElevated, CharcoalElevated))
        else -> Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bgBrush)
            .border(
                width = if (selected && !useCrimsonGradient) 1.dp else 0.dp,
                color = if (selected && !useCrimsonGradient) accentColor.copy(alpha = 0.55f) else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = OrbitronFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = when {
                selected && useCrimsonGradient -> Color.White
                selected -> accentColor
                else -> TextMuted
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MissionSectionHeader(
    icon: ImageVector,
    title: String,
    countLabel: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp,
                color = accentColor
            )
        }
        Text(
            text = "$countLabel CLAIMED",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
    }
}

@Composable
private fun MissionCard(
    tagId: String,
    title: String,
    description: String,
    icon: ImageVector,
    progress: Int,
    target: Int,
    rewardUsd: Int,
    claimed: Boolean,
    accentColor: Color,
    highlighted: Boolean,
    onClaim: () -> Unit
) {
    val complete = progress >= target
    val frac = (progress.toFloat() / target.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val cardAccent = when {
        claimed -> TextMuted
        complete -> EmeraldAccent
        else -> accentColor
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (highlighted || (!claimed && complete)) CharcoalElevated else CharcoalPanel)
            .border(
                width = if ((!claimed && complete) || highlighted) 1.3.dp else 1.dp,
                color = when {
                    claimed -> CharcoalHairline
                    complete -> EmeraldAccent.copy(alpha = 0.65f)
                    highlighted -> AmberAccent.copy(alpha = 0.45f)
                    else -> CharcoalHairline
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .testTag("mission_card_$tagId"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardAccent.copy(alpha = 0.14f))
                    .border(1.dp, cardAccent.copy(alpha = 0.42f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = cardAccent, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (claimed) TextMuted else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when {
                claimed -> StatusTagPill(text = "CLAIMED", accentColor = TextMuted)
                complete -> StatusTagPill(text = "COMPLETE", accentColor = EmeraldAccent)
                else -> Text(
                    text = "+$${String.format(Locale.US, "%,d", rewardUsd)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberAccent
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = cardAccent,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${progress.coerceAtMost(target)}/$target",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (claimed) TextMuted else TextPrimary
            )
        }

        if (!claimed && complete) {
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent,
                    contentColor = CharcoalBg
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("claim_$tagId")
            ) {
                Text(
                    text = "CLAIM +$${String.format(Locale.US, "%,d", rewardUsd)} USD REWARD",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun EmptyRunsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CharcoalPanel)
                .border(1.dp, CharcoalHairline, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = AmberAccent,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "NO RUNS RECORDED YET",
                fontFamily = OrbitronFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hit the highway to log your first best run, earn USD, and populate local telemetry.",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun RunHistoryCard(
    rank: Int,
    run: RunRecordEntity,
    modifier: Modifier = Modifier
) {
    val rankColor = when (rank) {
        1 -> AmberAccent
        2 -> CyanAccent
        3 -> MagentaAccent
        else -> TextMuted
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalPanel)
            .border(
                width = if (rank == 1) 1.3.dp else 1.dp,
                color = if (rank == 1) AmberAccent.copy(alpha = 0.55f) else CharcoalHairline,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusTagPill(text = "#$rank", accentColor = rankColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format(Locale.US, "%,d PTS", run.score),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanAccent
                )
            }
            StatusTagPill(
                text = "+$${String.format(Locale.US, "%,d", run.usdEarned)}",
                accentColor = EmeraldAccent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${run.bikeName} · ${run.routeName}",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatusTagPill(
                text = "${run.difficulty} · ${run.trafficMode}",
                accentColor = MagentaAccent
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RunTelemetryChip("DIST", formatDist(run.distanceMeters), Modifier.weight(1f))
            RunTelemetryChip("TOP", "${run.topSpeedKmh} KM/H", Modifier.weight(1f))
            RunTelemetryChip("NEAR", "${run.nearMisses}", Modifier.weight(0.7f))
            RunTelemetryChip("END", run.endReason, Modifier.weight(0.9f))
        }
    }
}

@Composable
private fun RunTelemetryChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalElevated)
            .padding(horizontal = 7.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
