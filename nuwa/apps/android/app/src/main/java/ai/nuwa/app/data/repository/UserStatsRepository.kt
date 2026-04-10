package ai.nuwa.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class OperationLogEntry(
    val id: String,
    val timestamp: Long,
    val action: String,
    val status: OperationStatus,
    val detail: String,
    val intentType: String = ""
)

enum class OperationStatus {
    Success,
    Failed,
    Running
}

data class UserStats(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val failedTasks: Int = 0,
    val level: Int = 1,
    val experience: Int = 0,
    val learnedSkills: List<String> = emptyList(),
    val operationLogs: List<OperationLogEntry> = emptyList(),
    val firstUseDate: Long = System.currentTimeMillis()
)

class UserStatsRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "nuwa_user_stats"
        private const val KEY_USER_STATS = "user_stats"
        private const val KEY_OPERATION_LOGS = "operation_logs"
        private const val MAX_LOG_ENTRIES = 100
    }
    
    fun getUserStats(): UserStats {
        val json = prefs.getString(KEY_USER_STATS, null) ?: return UserStats()
        return try {
            val obj = JSONObject(json)
            UserStats(
                totalTasks = obj.optInt("totalTasks", 0),
                completedTasks = obj.optInt("completedTasks", 0),
                failedTasks = obj.optInt("failedTasks", 0),
                level = obj.optInt("level", 1),
                experience = obj.optInt("experience", 0),
                learnedSkills = parseLearnedSkills(obj.optJSONArray("learnedSkills")),
                firstUseDate = obj.optLong("firstUseDate", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            UserStats()
        }
    }
    
    private fun parseLearnedSkills(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return try {
            (0 until array.length()).map { array.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveUserStats(stats: UserStats) {
        val obj = JSONObject().apply {
            put("totalTasks", stats.totalTasks)
            put("completedTasks", stats.completedTasks)
            put("failedTasks", stats.failedTasks)
            put("level", stats.level)
            put("experience", stats.experience)
            put("learnedSkills", JSONArray(stats.learnedSkills))
            put("firstUseDate", stats.firstUseDate)
        }
        prefs.edit().putString(KEY_USER_STATS, obj.toString()).apply()
    }
    
    fun recordTaskCompletion(success: Boolean, intentType: String) {
        val stats = getUserStats()
        val newStats = stats.copy(
            totalTasks = stats.totalTasks + 1,
            completedTasks = if (success) stats.completedTasks + 1 else stats.completedTasks,
            failedTasks = if (!success) stats.failedTasks + 1 else stats.failedTasks,
            experience = stats.experience + if (success) 10 else 3,
            level = calculateLevel(stats.experience + if (success) 10 else 3)
        )
        saveUserStats(newStats)
    }
    
    fun calculateLevel(experience: Int): Int {
        return when {
            experience < 50 -> 1
            experience < 150 -> 2
            experience < 300 -> 3
            experience < 500 -> 4
            experience < 800 -> 5
            experience < 1200 -> 6
            experience < 1700 -> 7
            experience < 2300 -> 8
            experience < 3000 -> 9
            else -> 10
        }
    }
    
    fun addOperationLog(action: String, status: OperationStatus, detail: String, intentType: String = "") {
        val logs = getOperationLogs().toMutableList()
        
        val newLog = OperationLogEntry(
            id = "log_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            action = action,
            status = status,
            detail = detail,
            intentType = intentType
        )
        
        logs.add(0, newLog)
        
        if (logs.size > MAX_LOG_ENTRIES) {
            logs.subList(MAX_LOG_ENTRIES, logs.size).clear()
        }
        
        saveOperationLogs(logs)
    }
    
    fun getOperationLogs(): List<OperationLogEntry> {
        val json = prefs.getString(KEY_OPERATION_LOGS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                OperationLogEntry(
                    id = obj.getString("id"),
                    timestamp = obj.getLong("timestamp"),
                    action = obj.getString("action"),
                    status = OperationStatus.valueOf(obj.getString("status")),
                    detail = obj.getString("detail"),
                    intentType = obj.optString("intentType", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveOperationLogs(logs: List<OperationLogEntry>) {
        val array = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("id", log.id)
                put("timestamp", log.timestamp)
                put("action", log.action)
                put("status", log.status.name)
                put("detail", log.detail)
                put("intentType", log.intentType)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_OPERATION_LOGS, array.toString()).apply()
    }
    
    fun clearOperationLogs() {
        prefs.edit().remove(KEY_OPERATION_LOGS).apply()
    }
    
    fun getCompletionRate(): Float {
        val stats = getUserStats()
        return if (stats.totalTasks > 0) {
            stats.completedTasks.toFloat() / stats.totalTasks.toFloat()
        } else 0f
    }
    
    fun getUsedDays(): Int {
        val stats = getUserStats()
        val diff = System.currentTimeMillis() - stats.firstUseDate
        return ((diff / (1000 * 60 * 60 * 24)) + 1).toInt()
    }
}