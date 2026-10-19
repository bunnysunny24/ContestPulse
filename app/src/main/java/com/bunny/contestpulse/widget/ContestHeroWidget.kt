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
import com.bunny.contestpulse.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 2x2 Square Bento Hero Widget for ContestPulse.
 * Designed like the compact square bento tiles in reference designs,
 * featuring a prominent countdown and platform capsule.
 */
class ContestHeroWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val nextContest = withContext(Dispatchers.IO) {
            try {
                val db = ContestDatabase.getInstance(context)
                val now = System.currentTimeMillis()
                db.contestDao().getNextContests(currentTime = now, limit = 1).firstOrNull()
            } catch (e: Exception) {
                null
            }
        }

        provideContent {
            HeroSquareWidgetContent(contest = nextContest)
        }
    }
}

@Composable
fun HeroSquareWidgetContent(contest: Contest?) {
    val bentoBase = Color(0xFF0F1014)          // Deep Obsidian Bento Base
    val neonLime = ColorProvider(Color(0xFFD8F862))
    val textMuted = ColorProvider(Color(0xFF8E929E))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bentoBase)
            .cornerRadius(24.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        if (contest != null) {
            val countdown = formatWidgetCountdown(contest.timeUntilStart())
            val timeStr = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
                .format(Date(contest.startTimeMillis))
            val (tagText, tagColor) = getPlatformTag(contest.platform)

            // Top Pill Capsule Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(10.dp)
                        .background(Color(0xFF1E202B))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ NEXT",
                        style = TextStyle(
                            color = neonLime,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Box(
                    modifier = GlanceModifier
                        .cornerRadius(8.dp)
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tagText,
                        style = TextStyle(
                            color = ColorProvider(tagColor),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Label
            Text(
                text = "STARTS IN",
                style = TextStyle(
                    color = textMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            // Giant Countdown Metric (Like 32K or 5,548 in reference designs)
            Text(
                text = countdown,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Contest Name
            Text(
                text = contest.name,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFF4F4F5)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Time and Alarm status
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeStr,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFA1A1AA)),
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = "🔔 ON",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF818CF8)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        } else {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No upcoming\ncontests",
                    style = TextStyle(
                        color = textMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
