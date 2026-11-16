package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RunRecordEntity
import com.example.model.CareerMilestone
import com.example.model.DailyMission
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
fun MissionsAndRunsSheet(
    dailyMissions: List<DailyMission>,
    milestones: List<CareerMilestone>,
    topRuns: List<RunRecordEntity>,
    onClaimDailyMission: (DailyMission) -> Unit,
    onClaimMilestone: (CareerMilestone) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    var tab by remember { mutableIntStateOf(0) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(48.dp).testTag("missions_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "MISSIONS & BEST RUNS",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "EARN BONUS USD REWARDS · OFFLINE LEADERBOARD",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            TabRow(
                selectedTabIndex = tab,
                containerColor = SurfaceCard,
                contentColor = BoostCyan,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = {
                        Text("MISSIONS", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                    }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = {
                        Text("BEST RUNS", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.Bold)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "DAILY MISSIONS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BoostCyan
                        )
                    }
                    items(dailyMissions, key = { it.id }) { m ->
                        MissionCard(
                            title = m.title,
                            description = m.description,
                            progress = m.progress,
                            target = m.target,
                            rewardUsd = m.rewardUsd,
                            claimed = m.claimed,
                            onClaim = { onClaimDailyMission(m) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "CAREER MILESTONES",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberGold
                        )
                    }
                    items(milestones, key = { it.id }) { ms ->
                        MissionCard(
                            title = ms.title,
                            description = ms.description,
                            progress = ms.progress,
                            target = ms.target,
                            rewardUsd = ms.rewardUsd,
                            claimed = ms.claimed,
                            onClaim = { onClaimMilestone(ms) }
                        )
                    }
                }
            } else {
                if (topRuns.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = AmberGold, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "NO RUNS RECORDED YET",
                                fontFamily = OrbitronFontFamily,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Hit the highway to log your first best run and earn USD.",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(topRuns, key = { it.id }) { run ->
                            RunHistoryCard(run)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    title: String,
    description: String,
    progress: Int,
    target: Int,
    rewardUsd: Int,
    claimed: Boolean,
    onClaim: () -> Unit
) {
    val complete = progress >= target
    val frac = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                when {
                    claimed -> Color.White.copy(alpha = 0.08f)
                    complete -> CashGreen.copy(alpha = 0.65f)
                    else -> Color.White.copy(alpha = 0.12f)
                },
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontFamily = OrbitronFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, fontSize = 12.sp, color = TextMuted)
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (claimed) {
                Text("CLAIMED", fontFamily = JetBrainsMonoFontFamily, fontSize = 11.sp, color = TextMuted)
            } else if (complete) {
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = CashGreen, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("CLAIM $$rewardUsd", fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            } else {
                Text(
                    text = "+$${String.format(Locale.US, "%,d", rewardUsd)}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberGold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { frac },
                modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)),
                color = if (complete) CashGreen else BoostCyan,
                trackColor = Color.White.copy(alpha = 0.08f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${progress.coerceAtMost(target)}/$target",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun RunHistoryCard(run: RunRecordEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceElevated)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format(Locale.US, "%,d PTS", run.score),
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BoostCyan
            )
            Text(
                text = "+$${String.format(Locale.US, "%,d", run.usdEarned)} USD",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CashGreen
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${run.bikeName} · ${run.routeName}",
                fontSize = 12.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${run.difficulty} · ${run.trafficMode}",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = NeonPink
            )
        }
        Text(
            text = "${formatDist(run.distanceMeters)} · TOP ${run.topSpeedKmh} KM/H · ${run.nearMisses} NEAR MISSES · ${run.endReason}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}
