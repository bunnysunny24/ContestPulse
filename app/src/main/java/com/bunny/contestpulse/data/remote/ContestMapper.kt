package com.bunny.contestpulse.data.remote

import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.ContestPhase
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.data.remote.dto.CodeChefContestItem
import com.bunny.contestpulse.data.remote.dto.CodeforcesContest
import com.bunny.contestpulse.data.remote.dto.LeetCodeContest
import java.text.SimpleDateFormat
import java.util.*

object ContestMapper {

    fun fromCodeforces(cf: CodeforcesContest): Contest {
        val phase = when (cf.phase) {
            "BEFORE" -> ContestPhase.UPCOMING
            "CODING" -> ContestPhase.RUNNING
            else -> ContestPhase.FINISHED
        }

        return Contest(
            id = "cf_${cf.id}",
            name = cf.name,
            platform = Platform.CODEFORCES,
            phase = phase,
            startTimeMillis = (cf.startTimeSeconds ?: 0) * 1000L,
            durationMillis = cf.durationSeconds * 1000L,
            url = "https://codeforces.com/contest/${cf.id}"
        )
    }

    fun fromLeetCode(lc: LeetCodeContest): Contest {
        val startMillis = lc.startTime * 1000L
        val now = System.currentTimeMillis()
        val endMillis = startMillis + (lc.duration * 1000L)

        val phase = when {
            now < startMillis -> ContestPhase.UPCOMING
            now in startMillis..endMillis -> ContestPhase.RUNNING
            else -> ContestPhase.FINISHED
        }

        val slug = lc.titleSlug ?: lc.title.lowercase()
            .replace(" ", "-")
            .replace(Regex("[^a-z0-9-]"), "")

        return Contest(
            id = "lc_${slug}",
            name = lc.title,
            platform = Platform.LEETCODE,
            phase = phase,
            startTimeMillis = startMillis,
            durationMillis = lc.duration * 1000L,
            url = "https://leetcode.com/contest/$slug"
        )
    }

    fun fromCodeChef(item: CodeChefContestItem): Contest? {
        val name = item.contestName ?: return null
        val code = item.contestCode ?: item.contestId ?: return null
        val startMillis = parseIsoDate(item.startDateIso) ?: return null

        val durationMinutes = item.durationMinutes?.toLongOrNull() ?: 120L
        val durationMillis = durationMinutes * 60 * 1000L
        val now = System.currentTimeMillis()

        val phase = when {
            now < startMillis -> ContestPhase.UPCOMING
            now in startMillis..(startMillis + durationMillis) -> ContestPhase.RUNNING
            else -> ContestPhase.FINISHED
        }

        return Contest(
            id = "cc_${code}",
            name = name,
            platform = Platform.CODECHEF,
            phase = phase,
            startTimeMillis = startMillis,
            durationMillis = durationMillis,
            url = "https://www.codechef.com/$code"
        )
    }

    private fun parseIsoDate(dateStr: String?): Long? {
        if (dateStr == null) return null
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US)
        )
        for (format in formats) {
            try {
                val parsed = format.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (e: Exception) {
                // Try next
            }
        }
        return null
    }
}
