package com.bunny.contestpulse.alarm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunny.contestpulse.ui.theme.ContestPulseTheme

/**
 * Full-screen alarm activity that appears when a contest starts.
 * Shows on lock screen and wakes the device.
 */
class AlarmActivity : ComponentActivity() {

    private var contestId: String = ""
    private var contestName: String = ""
    private var contestStart: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on and show on lock screen
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        contestId = intent.getStringExtra("contest_id") ?: ""
        contestName = intent.getStringExtra("contest_name") ?: "Contest"
        contestStart = intent.getLongExtra("contest_start", 0)

        setContent {
            ContestPulseTheme {
                AlarmScreen(
                    contestName = contestName,
                    onDismiss = { dismissAlarm() },
                    onOpenContest = { openContest() }
                )
            }
        }
    }

    private fun dismissAlarm() {
        val stopIntent = Intent(this, AlarmService::class.java)
        stopService(stopIntent)
        finish()
    }

    private fun openContest() {
        dismissAlarm()
        // Determine URL from contest ID
        val url = when {
            contestId.startsWith("cf_") -> "https://codeforces.com/contest/${contestId.removePrefix("cf_")}"
            contestId.startsWith("lc_") -> "https://leetcode.com/contest/${contestId.removePrefix("lc_")}"
            contestId.startsWith("cc_") -> "https://www.codechef.com/${contestId.removePrefix("cc_")}"
            else -> return
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@Composable
fun AlarmScreen(
    contestName: String,
    onDismiss: () -> Unit,
    onOpenContest: () -> Unit
) {
    // Pulsing animation for the alarm icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09090B),
                        Color(0xFF14141E),
                        Color(0xFF09090B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            // Pulsing alarm icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .background(
                        Color(0xFF818CF8).copy(alpha = glowAlpha),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm",
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            // Title
            Text(
                text = "⚡ CONTEST STARTING",
                color = Color(0xFF818CF8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            // Contest name
            Text(
                text = contestName,
                color = Color(0xFFF4F4F5),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Your round is starting right now!",
                color = Color(0xFFA1A1AA),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Open Contest button
            Button(
                onClick = onOpenContest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF818CF8)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    Icons.Default.OpenInBrowser,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Open Contest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Dismiss button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFA1A1AA)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Dismiss",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
