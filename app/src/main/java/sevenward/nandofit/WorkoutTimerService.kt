package sevenward.nandofit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class WorkoutTimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        
        const val EXTRA_TIME_MILLIS = "EXTRA_TIME_MILLIS"
        const val EXTRA_EXERCISE_NAME = "EXTRA_EXERCISE_NAME"
        
        const val CHANNEL_ID = "workout_timer_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val timeMillis = intent.getLongExtra(EXTRA_TIME_MILLIS, 0L)
                val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Descanso"
                startTimerForeground(timeMillis, exerciseName)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimerForeground(timeMillis: Long, exerciseName: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Temporizador de Descanso",
                NotificationManager.IMPORTANCE_LOW // Low para que no suene cada segundo, pero se actualiza en pantalla
            ).apply {
                description = "Muestra el tiempo de descanso restante"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Botón para detener el temporizador
        val stopIntent = Intent(this, WorkoutTimerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Calculamos la hora base para el chronometer
        // base = SystemClock.elapsedRealtime() + tiempo que falta
        val baseTime = SystemClock.elapsedRealtime() + timeMillis

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.logo_nandofitplus) // Asegúrate de usar un icono adecuado, el sistema prefiere iconos monocromáticos
            .setContentTitle("Descanso: $exerciseName")
            .setContentText("Tiempo restante")
            .setOngoing(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(baseTime)
            .setContentIntent(pendingIntent)
            .addAction(0, "Detener", stopPendingIntent)
            .setCategory(Notification.CATEGORY_PROGRESS)
            
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }
}
