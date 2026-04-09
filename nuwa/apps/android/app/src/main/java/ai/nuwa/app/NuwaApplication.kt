package ai.nuwa.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NuwaApplication : Application() {

    companion object {
        const val CHANNEL_ID = "nuwa_core_service"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "女娲核心服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "女娲智能助手后台运行通道"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
