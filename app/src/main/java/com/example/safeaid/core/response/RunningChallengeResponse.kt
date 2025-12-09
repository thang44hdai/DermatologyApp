package com.example.safeaid.core.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RunningChallengeResponse(
    @SerialName("total_distance")
    var totalDistance: Double?,
    @SerialName("total_runs")
    var totalRuns: Int?,
    @SerialName("weekly_distance")
    var weeklyDistance: Double?,
    @SerialName("weekly_goal_distance")
    var weeklyGoalDistance: Double?,
    @SerialName("today_steps")
    var todaySteps: Int?,
    @SerialName("today_distance")
    var todayDistance: Double?,
    @SerialName("today_calories")
    var todayCalories: Int?
)
