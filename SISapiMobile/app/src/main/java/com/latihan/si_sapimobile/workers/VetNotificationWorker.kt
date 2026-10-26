package com.latihan.si_sapimobile.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.latihan.si_sapimobile.helpers.NotificationHelper
import com.latihan.si_sapimobile.helpers.SessionManager
import com.latihan.si_sapimobile.network.ApiClient
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class VetNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val WORK_NAME = "VetNotificationWork"
        private const val PREFS_NAME = "vet_notif_tracker"
        private const val KEY_NOTIFIED_EMERGENCIES = "notified_emergencies"
        private const val KEY_NOTIFIED_VACCINES = "notified_vaccines"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<VetNotificationWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun triggerImmediately(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<VetNotificationWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueue(request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val role = session.getUserRole().lowercase()

        if (role != "vet" || session.getToken().isNullOrBlank()) {
            return Result.success()
        }

        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifiedEmergencies = prefs.getStringSet(KEY_NOTIFIED_EMERGENCIES, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        val notifiedVaccines = prefs.getStringSet(KEY_NOTIFIED_VACCINES, emptySet())?.toMutableSet()
            ?: mutableSetOf()

        val api = ApiClient.getApiService(applicationContext)

        try {
            val emergencyRes = api.getEmergencyReports()
            if (emergencyRes.isSuccessful) {
                emergencyRes.body()?.forEach { report ->
                    val idKey = "emg_${report.id}_${report.date}"
                    if (!notifiedEmergencies.contains(idKey)) {
                        val tag = report.livestock?.tagNumber ?: "—"
                        val diag = report.diagnosis?.takeIf { it.isNotBlank() } ?: "Kondisi darurat dilaporkan."
                        NotificationHelper.showEmergencyNotification(applicationContext, tag, diag)
                        notifiedEmergencies.add(idKey)
                    }
                }
            }

            val vaccineRes = api.getDueVaccinations()
            if (vaccineRes.isSuccessful) {
                val today = LocalDate.now()
                vaccineRes.body()?.forEach { vaccine ->
                    val due = vaccine.nextDueDate?.take(10)
                    val idKey = "vac_${vaccine.id}_$due"
                    if (!due.isNullOrBlank() && !notifiedVaccines.contains(idKey)) {
                        try {
                            val dueDate = LocalDate.parse(due)
                            if (!dueDate.isAfter(today)) {
                                val tag = vaccine.livestock?.tagNumber ?: "—"
                                val treatment = vaccine.treatment ?: vaccine.diagnosis ?: "Vaksinasi Sapi"
                                NotificationHelper.showVaccineNotification(applicationContext, tag, treatment, due)
                                notifiedVaccines.add(idKey)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            prefs.edit()
                .putStringSet(KEY_NOTIFIED_EMERGENCIES, notifiedEmergencies)
                .putStringSet(KEY_NOTIFIED_VACCINES, notifiedVaccines)
                .apply()

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
