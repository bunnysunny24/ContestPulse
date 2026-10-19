package com.bunny.contestpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.ui.theme.*

@Composable
fun PlatformFilter(
    selectedPlatform: Platform?,
    onPlatformSelected: (Platform?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Frosted Capsule Container
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassSurface.copy(alpha = 0.8f))
            .border(1.dp, GlassBorderSubtle, RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilterSegment(
            title = "All",
            dotColor = Color.White,
            isSelected = selectedPlatform == null,
            onClick = { onPlatformSelected(null) },
            modifier = Modifier.weight(1f)
        )
        FilterSegment(
            title = "Codeforces",
            dotColor = CodeforcesBlue,
            isSelected = selectedPlatform == Platform.CODEFORCES,
            onClick = { onPlatformSelected(Platform.CODEFORCES) },
            modifier = Modifier.weight(1.3f)
        )
        FilterSegment(
            title = "CodeChef",
            dotColor = CodeChefOrange,
            isSelected = selectedPlatform == Platform.CODECHEF,
            onClick = { onPlatformSelected(Platform.CODECHEF) },
            modifier = Modifier.weight(1.2f)
        )
        FilterSegment(
            title = "LeetCode",
            dotColor = LeetCodeYellow,
            isSelected = selectedPlatform == Platform.LEETCODE,
            onClick = { onPlatformSelected(Platform.LEETCODE) },
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun FilterSegment(
    title: String,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgAnim by animateColorAsState(
        targetValue = if (isSelected) GlassSurfacePill else Color.Transparent,
        label = "bgAnim"
    )
    val textAnim by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextMuted,
        label = "textAnim"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgAnim)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing miniature dot
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isSelected) dotColor else dotColor.copy(alpha = 0.35f))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = textAnim,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
