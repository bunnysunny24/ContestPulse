package com.bunny.contestpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.ui.theme.*
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun NextContestHero(
    contest: Contest?,
    modifier: Modifier = Modifier
) {
    if (contest == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(GlassSurface.copy(alpha = 0.7f))
                .border(1.dp, GlassBorderSubtle, RoundedCornerShape(26.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No upcoming contests found\nPull down to refresh",
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
        }
        return
    }

    val platformColor = getPlatformColor(contest.platform)
    var timeRemaining by remember { mutableLongStateOf(contest.timeUntilStart()) }

    LaunchedEffect(contest.id) {
        while (true) {
            timeRemaining = contest.timeUntilStart()
            delay(1000)
        }
    }

    val days = TimeUnit.MILLISECONDS.toDays(timeRemaining)
    val hours = TimeUnit.MILLISECONDS.toHours(timeRemaining) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemaining) % 60

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        platformColor.copy(alpha = 0.12f),
                        GlassSurfaceHigh.copy(alpha = 0.85f),
                        GlassSurface.copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.dp, GlassBorderBrush, RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top status pill bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform chip pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(platformColor.copy(alpha = 0.15f))
                        .border(1.dp, platformColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(platformColor)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = getPlatformName(contest.platform),
                        color = platformColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Live status dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (timeRemaining > 0) EmeraldLive else ErrorRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (timeRemaining > 0) "UPCOMING" else "LIVE NOW",
                        color = if (timeRemaining > 0) EmeraldLive else ErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Contest Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = contest.name,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Text(
                    text = "${formatContestDate(contest.startTimeMillis)} at ${formatContestTime(contest.startTimeMillis)} · Duration: ${formatDuration(contest.durationMillis)}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Minimalist Modern Glass Capsule Countdown
            if (timeRemaining > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, GlassBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (days > 0) {
                        ModernCountdownPill(days.toString().padStart(2, '0'), "DAYS", platformColor)
                        CountdownDivider()
                    }
                    ModernCountdownPill(hours.toString().padStart(2, '0'), "HOURS", platformColor)
                    CountdownDivider()
                    ModernCountdownPill(minutes.toString().padStart(2, '0'), "MINS", platformColor)
                    CountdownDivider()
                    ModernCountdownPill(seconds.toString().padStart(2, '0'), "SECS", platformColor)
                }
            }
        }
    }
}

@Composable
private fun ModernCountdownPill(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = label,
            color = TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CountdownDivider() {
    Text(
        text = ":",
        color = TextMuted.copy(alpha = 0.4f),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}
