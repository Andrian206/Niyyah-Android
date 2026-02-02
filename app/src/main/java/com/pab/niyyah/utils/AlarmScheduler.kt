package com.pab.niyyah.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {

    fun scheduleAlarm(context: Context, taskTitle: String, taskDetails: String, dateStr: String, timeStr: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Parse Tanggal & Waktu menjadi Milliseconds
        val timeInMillis = getTimeInMillis(dateStr, timeStr)

        if (timeInMillis <= System.currentTimeMillis()) {
            // Jangan jadwalkan jika waktu sudah lewat
            return
        }

        // 2. Siapkan Data yang mau dikirim ke Receiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_DETAILS", taskDetails)
            // Gunakan HashCode judul sebagai ID unik notifikasi (sederhana)
            putExtra("TASK_ID", taskTitle.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskTitle.hashCode(), // ID Request unik
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Pasang Alarm (SetExact agar tepat waktu)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
            Log.d("Alarm", "Alarm diset untuk: $dateStr $timeStr")
        } catch (e: SecurityException) {
            Toast.makeText(context, "Ijin alarm tidak diberikan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTimeInMillis(dateStr: String, timeStr: String): Long {
        return try {
            // Sesuaikan format ini dengan output DatePicker & TimePicker Anda
            // Contoh: Date="23/01/2026", Time="08:30 AM"
            val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)
            val date = format.parse("$dateStr $timeStr")
            date?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    // Fungsi membatalkan alarm (misal kalau task dihapus/diedit)
    fun cancelAlarm(context: Context, taskTitle: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}