package com.bunny.contestpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ContestCard(
    contest: Contest,
    onAlarmToggle: (Boolean) -> Unit,
    onOpenContest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platformColor = getPlatformColor(contest.platform)
    val timeUntilStart = contest.timeUntilStart()
    val countdownText = formatCountdown(timeUntilStart)
    val dateText = formatContestDate(contest.startTimeMillis)
    val timeText = formatContestTime(contest.startTimeMillis)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassSurfaceHigh.copy(alpha = 0.9f),
                        GlassSurface.copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.dp, GlassBorderBrush, RoundedCornerShape(22.dp))
            .clickable { onOpenContest() }
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Platform Pill & Frosted Bell Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform Tag Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(platformColor.copy(alpha = 0.12f))
                        .border(1.dp, platformColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(platformColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = getPlatformName(contest.platform),
                        color = platformColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                // Frosted Circular Alarm Toggle
                val alarmBg by animateColorAsState(
                    targetValue = if (contest.isAlarmEnabled) platformColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                    label = "alarmBg"
                )
                val alarmIconColor by animateColorAsState(
                    targetValue = if (contest.isAlarmEnabled) platformColor else TextMuted,
                    label = "alarmColor"
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(alarmBg)
                        .border(1.dp, GlassBorderSubtle, CircleShape)
                        .clickable { onAlarmToggle(!contest.isAlarmEnabled) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (contest.isAlarmEnabled)
                            Icons.Outlined.Notifications
                        else
                            Icons.Outlined.NotificationsOff,
                        contentDescription = "Toggle alarm",
                        tint = alarmIconColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Contest Name
            Text(
                text = contest.name,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            // Bottom Info Bar: Date/Time + Countdown Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time & Duration
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "$dateText · $timeText",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Duration: ${formatDuration(contest.durationMillis)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                // Countdown Capsule
                if (timeUntilStart > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(platformColor.copy(alpha = 0.10f))
                            .border(1.dp, platformColor.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = countdownText,
                            color = platformColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldLive.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldLive.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LIVE NOW",
                            color = EmeraldLive,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

fun getPlatformColor(platform: Platform): Color = when (platform) {
    Platform.CODEFORCES -> CodeforcesBlue
    Platform.CODECHEF -> CodeChefOrange
    Platform.LEETCODE -> LeetCodeYellow
}

fun getPlatformName(platform: Platform): String = when (platform) {
    Platform.CODEFORCES -> "CODEFORCES"
    Platform.CODECHEF -> "CODECHEF"
    Platform.LEETCODE -> "LEETCODE"
}

fun formatCountdown(millis: Long): String {
    if (millis <= 0) return "Live"
    val days = TimeUnit.MILLISECONDS.toDays(millis)
    val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        append("${minutes}m")
    }.trim()
}

fun formatContestDate(millis: Long): String {
    val now = Calendar.getInstance()
    val contest = Calendar.getInstance().apply { timeInMillis = millis }

    return when {
        now.get(Calendar.DATE) == contest.get(Calendar.DATE) &&
                now.get(Calendar.YEAR) == contest.get(Calendar.YEAR) -> "Today"
        now.get(Calendar.DATE) + 1 == contest.get(Calendar.DATE) &&
                now.get(Calendar.YEAR) == contest.get(Calendar.YEAR) -> "Tomorrow"
        else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(millis))
    }
}

fun formatContestTime(millis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
}

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
}
