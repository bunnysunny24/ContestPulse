package com.bunny.contestpulse.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.bunny.contestpulse.data.local.ContestDatabase
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Bento Box Grid Widget (4x2 / 4x3) for ContestPulse.
 * Inspired directly by modern Material You and Nothing OS bento modular designs.
 * Compact, zero-clipping layout with a prominent Hero countdown and stacked platform tiles.
 */
class ContestWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val contests = withContext(Dispatchers.IO) {
            try {
                val db = ContestDatabase.getInstance(context)
                val now = System.currentTimeMillis()
                db.contestDao().getNextContests(currentTime = now, limit = 15)
            } catch (e: Exception) {
                emptyList()
            }
        }

        // Get the next contest per platform, then sort by earliest start time
        val nextByPlatform = contests
            .groupBy { it.platform }
            .mapValues { it.value.first() }
            .values
            .sortedBy { it.startTimeMillis }

        val heroContest = nextByPlatform.firstOrNull()
        val secondaryContests = nextByPlatform.drop(1)

        provideContent {
            BentoGridWidgetContent(
                heroContest = heroContest,
                secondaryContests = secondaryContests,
                totalUpcomingCount = contests.size
            )
        }
    }
}

@Composable
fun BentoGridWidgetContent(
    heroContest: Contest?,
    secondaryContests: List<Contest>,
    totalUpcomingCount: Int
) {
    val bentoBase = Color(0xFF0F1014)          // Deep Obsidian Bento Base
    val neonLime = ColorProvider(Color(0xFFD8F862))
    val textMuted = ColorProvider(Color(0xFF71717A))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bentoBase)
            .cornerRadius(22.dp)
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        // --- Bento Top Header Bar ---
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Neon pill tag (Like EXPENSES / PERFORMANCE in reference designs)
            Box(
                modifier = GlanceModifier
                    .cornerRadius(10.dp)
                    .background(Color(0xFF1E202B))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡ CONTESTPULSE",
                    style = TextStyle(
                        color = neonLime,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Live status tag
            Box(
                modifier = GlanceModifier
                    .cornerRadius(10.dp)
                    .background(Color(0xFF1A1C25))
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                val countText = if (totalUpcomingCount > 0) "● $totalUpcomingCount UPCOMING" else "● LIVE"
                Text(
                    text = countText,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF34D399)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(5.dp))

        // --- Bento Body: Asymmetric 2-Column Modular Grid ---
        if (heroContest != null) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT HERO BENTO TILE (Primary focus with giant countdown)
                BentoHeroCard(
                    contest = heroContest,
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                )

                Spacer(modifier = GlanceModifier.width(6.dp))

                // RIGHT STACKED BENTO TILES (Secondary platforms)
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight()
                ) {
                    val firstSecondary = secondaryContests.getOrNull(0)
                    val secondSecondary = secondaryContests.getOrNull(1)

                    if (firstSecondary != null) {
                        BentoSecondaryTile(
                            contest = firstSecondary,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                        )
                    }

                    if (secondSecondary != null) {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                        BentoSecondaryTile(
                            contest = secondSecondary,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                        )
                    } else if (firstSecondary == null) {
                        BentoSecondaryEmptyTile(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                        )
                    }
                }
            }
        } else {
            // Empty State Tile
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF161720))
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No upcoming contests found\nTap to open ContestPulse",
                    style = TextStyle(
                        color = textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

/**
 * Left Hero Bento Tile - Displays the next upcoming contest with a huge bold countdown.
 */
@Composable
fun BentoHeroCard(
    contest: Contest,
    modifier: GlanceModifier = GlanceModifier
) {
    val countdown = formatWidgetCountdown(contest.timeUntilStart())
    val timeStr = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
        .format(Date(contest.startTimeMillis))

    val (badgeText, badgeBg, badgeTextColor) = getPlatformHeroBadge(contest.platform)

    Column(
        modifier = modifier
            .background(Color(0xFF171822))
            .cornerRadius(16.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Top Pill Tag Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .cornerRadius(7.dp)
                    .background(badgeBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = TextStyle(
                        color = ColorProvider(badgeTextColor),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier
                    .cornerRadius(7.dp)
                    .background(Color(0xFF232532))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔔 ON",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF818CF8)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Subtitle Label
        Text(
            text = "STARTS IN",
            style = TextStyle(
                color = ColorProvider(Color(0xFF8E929E)),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Giant Bold Metric (Like 32K or $1248 in reference designs)
        Text(
            text = countdown,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        // Contest Title
        Text(
            text = contest.name,
            style = TextStyle(
                color = ColorProvider(Color(0xFFF4F4F5)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )

        // Time
        Text(
            text = timeStr,
            style = TextStyle(
                color = ColorProvider(Color(0xFFA1A1AA)),
                fontSize = 9.sp
            )
        )
    }
}

/**
 * Right Secondary Bento Tile - Stacked platform card with capsule tags.
 */
@Composable
fun BentoSecondaryTile(
    contest: Contest,
    modifier: GlanceModifier = GlanceModifier
) {
    val countdown = formatWidgetCountdown(contest.timeUntilStart())
    val timeStr = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
        .format(Date(contest.startTimeMillis))
    val (tagText, tagColor) = getPlatformTag(contest.platform)

    Column(
        modifier = modifier
            .background(Color(0xFF171822))
            .cornerRadius(14.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tag capsule
            Box(
                modifier = GlanceModifier
                    .cornerRadius(6.dp)
                    .background(Color(0x28FFFFFF))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tagText,
                    style = TextStyle(
                        color = ColorProvider(tagColor),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Countdown Pill
            Box(
                modifier = GlanceModifier
                    .cornerRadius(7.dp)
                    .background(Color(0xFF242635))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdown,
                    style = TextStyle(
                        color = ColorProvider(tagColor),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(3.dp))

        // Contest Name
        Text(
            text = contest.name,
            style = TextStyle(
                color = ColorProvider(Color(0xFFF4F4F5)),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )

        // Subtitle Time
        Text(
            text = timeStr,
            style = TextStyle(
                color = ColorProvider(Color(0xFF8E929E)),
                fontSize = 8.sp
            )
        )
    }
}

@Composable
fun BentoSecondaryEmptyTile(
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF171822))
            .cornerRadius(14.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No more contests\nscheduled",
            style = TextStyle(
                color = ColorProvider(Color(0xFF71717A)),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

fun getPlatformHeroBadge(platform: Platform): Triple<String, Color, Color> = when (platform) {
    Platform.CODEFORCES -> Triple("CODEFORCES", Color(0xFF38BDF8), Color(0xFF090A0F))
    Platform.CODECHEF -> Triple("CODECHEF", Color(0xFFFB923C), Color(0xFF090A0F))
    Platform.LEETCODE -> Triple("LEETCODE", Color(0xFFFBBF24), Color(0xFF090A0F))
}

fun getPlatformTag(platform: Platform): Pair<String, Color> = when (platform) {
    Platform.CODEFORCES -> Pair("CF", Color(0xFF38BDF8))
    Platform.CODECHEF -> Pair("CC", Color(0xFFFB923C))
    Platform.LEETCODE -> Pair("LC", Color(0xFFFBBF24))
}

fun formatWidgetCountdown(millis: Long): String {
    if (millis <= 0) return "LIVE"
    val days = TimeUnit.MILLISECONDS.toDays(millis)
    val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
