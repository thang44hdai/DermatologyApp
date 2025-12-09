package com.example.safeaid.core.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MorningExerciseChallengeResponse(
    @SerialName("streak")
    var streak: Int?,
    @SerialName("total_days")
    var totalDays: Int?,
    @SerialName("this_week_count")
    var thisWeekCount: Int?,
    @SerialName("checked_in_today")
    var checkedInToday: Boolean?
)
