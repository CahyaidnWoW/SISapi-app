package com.latihan.si_sapimobile.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.ui.ScanActivity

object NotificationHelper {
    private const val EMERGENCY_CHANNEL_ID = "sisapi_emergency_channel"
    private const val EMERGENCY_CHANNEL_NAME = "Laporan Darurat Sapi"
    private const val VACCINE_CHANNEL_ID = "sisapi_vaccine_channel"
    private const val VACCINE_CHANNEL_NAME = "Jadwal Vaksinasi Sapi"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val emergencyChannel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                EMERGENCY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi darurat sapi sakit untuk dokter hewan"
                enableVibration(true)
            }

            val vaccineChannel = NotificationChannel(
                VACCINE_CHANNEL_ID,
                VACCINE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat jadwal vaksinasi berkala sapi"
                enableVibration(true)
            }

            manager.createNotificationChannel(emergencyChannel)
            manager.createNotificationChannel(vaccineChannel)
        }
    }

    fun showEmergencyNotification(context: Context, tagNumber: String, diagnosis: String) {
        createNotificationChannels(context)

        val intent = Intent(context, ScanActivity::class.java).apply {
            putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_TREATMENT)
            putExtra(ScanActivity.EXTRA_INITIAL_TAG, tagNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("emergency_$tagNumber").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle("Darurat Sapi $tagNumber !!")
            .setContentText(diagnosis.ifBlank { "Sapi dilaporkan mengalami sakit di kandang." })
            .setStyle(NotificationCompat.BigTextStyle().bigText("Sapi $tagNumber: ${diagnosis.ifBlank { "Kondisi darurat dilaporkan oleh pekerja kandang." }}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(("emergency_$tagNumber").hashCode(), builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showVaccineNotification(context: Context, tagNumber: String, treatment: String, dueDate: String) {
        createNotificationChannels(context)

        val intent = Intent(context, ScanActivity::class.java).apply {
            putExtra(ScanActivity.EXTRA_MODE, ScanActivity.MODE_TREATMENT)
            putExtra(ScanActivity.EXTRA_INITIAL_TAG, tagNumber)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("vaccine_$tagNumber").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, VACCINE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_syringe)
            .setContentTitle("💉 Jadwal Vaksin: Sapi $tagNumber")
            .setContentText("$treatment • Jatuh tempo: $dueDate")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(("vaccine_$tagNumber").hashCode(), builder.build())
        } catch (_: SecurityException) {
        }
    }
}
