package com.example.safeaid.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class RunningDataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("running_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    data class RunningSession(
        val id: String = UUID.randomUUID().toString(),
        val date: String,
        val steps: Int,
        val distance: Double,
        val durationMinutes: Long,
        val calories: Int,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class DailyStats(
        val date: String,
        val totalSteps: Int,
        val totalDistance: Double,
        val totalCalories: Int,
        val sessionsCount: Int
    )
    
    // Save a running session
    fun saveSession(steps: Int, distance: Double, durationMinutes: Long, calories: Int) {
        val sessions = getAllSessions().toMutableList()
        val newSession = RunningSession(
            date = dateFormat.format(Date()),
            steps = steps,
            distance = distance,
            durationMinutes = durationMinutes,
            calories = calories
        )
        sessions.add(newSession)
        
        val json = gson.toJson(sessions)
        prefs.edit().putString("sessions", json).apply()
        
        // Update today's stats
        updateTodayStats()
    }
    
    // Get all sessions
    fun getAllSessions(): List<RunningSession> {
        val json = prefs.getString("sessions", null) ?: return emptyList()
        val type = object : TypeToken<List<RunningSession>>() {}.type
        return gson.fromJson(json, type)
    }
    
    // Get today's sessions
    fun getTodaySessions(): List<RunningSession> {
        val today = dateFormat.format(Date())
        return getAllSessions().filter { it.date == today }
    }
    
    // Get this week's sessions
    fun getWeekSessions(): List<RunningSession> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val weekStart = dateFormat.format(calendar.time)
        
        return getAllSessions().filter { it.date >= weekStart }
    }
    
    // Get today's stats
    fun getTodayStats(): DailyStats {
        val today = dateFormat.format(Date())
        val sessions = getTodaySessions()
        
        return DailyStats(
            date = today,
            totalSteps = sessions.sumOf { it.steps },
            totalDistance = sessions.sumOf { it.distance },
            totalCalories = sessions.sumOf { it.calories },
            sessionsCount = sessions.size
        )
    }
    
    // Get weekly stats
    fun getWeeklyStats(): Pair<Double, Double> {
        val weekSessions = getWeekSessions()
        val totalDistance = weekSessions.sumOf { it.distance }
        val goalDistance = getWeeklyGoal()
        return Pair(totalDistance, goalDistance)
    }
    
    // Get total stats
    fun getTotalStats(): Triple<Double, Int, Int> {
        val sessions = getAllSessions()
        val totalDistance = sessions.sumOf { it.distance }
        val totalRuns = sessions.size
        val totalCalories = sessions.sumOf { it.calories }
        return Triple(totalDistance, totalRuns, totalCalories)
    }
    
    // Update today's stats cache
    private fun updateTodayStats() {
        val stats = getTodayStats()
        prefs.edit()
            .putInt("today_steps", stats.totalSteps)
            .putString("today_distance", stats.totalDistance.toString())
            .putInt("today_calories", stats.totalCalories)
            .apply()
    }
    
    // Get weekly goal
    fun getWeeklyGoal(): Double {
        return prefs.getFloat("weekly_goal", 10.0f).toDouble()
    }
    
    // Set weekly goal
    fun setWeeklyGoal(goal: Double) {
        prefs.edit().putFloat("weekly_goal", goal.toFloat()).apply()
    }
    
    // Get achievements
    fun getAchievements(): List<Achievement> {
        val sessions = getAllSessions()
        val totalDistance = sessions.sumOf { it.distance }
        val totalRuns = sessions.size
        
        val achievements = mutableListOf<Achievement>()
        
        // First run
        if (totalRuns >= 1) {
            achievements.add(Achievement("🏃 Bước đầu tiên", "Hoàn thành lần chạy đầu tiên", true))
        } else {
            achievements.add(Achievement("🏃 Bước đầu tiên", "Hoàn thành lần chạy đầu tiên", false))
        }
        
        // 5 runs
        if (totalRuns >= 5) {
            achievements.add(Achievement("💪 Người kiên trì", "Hoàn thành 5 lần chạy", true))
        } else {
            achievements.add(Achievement("💪 Người kiên trì", "Hoàn thành 5 lần chạy (${totalRuns}/5)", false))
        }
        
        // 10km total
        if (totalDistance >= 10.0) {
            achievements.add(Achievement("🎯 Vận động viên", "Chạy tổng cộng 10km", true))
        } else {
            achievements.add(Achievement("🎯 Vận động viên", "Chạy tổng cộng 10km (${String.format("%.1f", totalDistance)}/10)", false))
        }
        
        // 50km total
        if (totalDistance >= 50.0) {
            achievements.add(Achievement("⭐ Siêu sao", "Chạy tổng cộng 50km", true))
        } else {
            achievements.add(Achievement("⭐ Siêu sao", "Chạy tổng cộng 50km (${String.format("%.1f", totalDistance)}/50)", false))
        }
        
        // 100 runs
        if (totalRuns >= 100) {
            achievements.add(Achievement("👑 Huyền thoại", "Hoàn thành 100 lần chạy", true))
        } else {
            achievements.add(Achievement("👑 Huyền thoại", "Hoàn thành 100 lần chạy (${totalRuns}/100)", false))
        }
        
        return achievements
    }
    
    data class Achievement(
        val title: String,
        val description: String,
        val unlocked: Boolean
    )
    
    // Clear all data (for testing)
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
