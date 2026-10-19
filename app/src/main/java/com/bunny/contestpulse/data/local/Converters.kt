package com.bunny.contestpulse.data.local

import androidx.room.TypeConverter
import com.bunny.contestpulse.data.model.ContestPhase
import com.bunny.contestpulse.data.model.Platform

/**
 * Room type converters for enum types.
 */
class Converters {
    @TypeConverter
    fun fromPlatform(platform: Platform): String = platform.name

    @TypeConverter
    fun toPlatform(value: String): Platform = Platform.valueOf(value)

    @TypeConverter
    fun fromContestPhase(phase: ContestPhase): String = phase.name

    @TypeConverter
    fun toContestPhase(value: String): ContestPhase = ContestPhase.valueOf(value)
}
