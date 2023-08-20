package com.e1120.schedulealarmworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.work.Data
import androidx.work.WorkManager
import com.e1120.schedulealarmworkmanager.AlertWorker.Companion.saveNotification
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)
    private val calendarActual = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)

    private var minute = 0
    private var hour = 0
    private var day = 0
    private var month = 0
    private var year = 0
    private var dateFormatted: String? = null
    private var tvNotificationDateTime: TextView? = null

    var btnClearNotification: Button? = null
    var btnSaveNotification: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sdfDateOnly = SimpleDateFormat("yyyy MM dd")
        tvNotificationDateTime = findViewById(R.id.notifications_date_time)
        btnClearNotification = findViewById(R.id.clear_notification)
        btnSaveNotification = findViewById(R.id.save_notification)

        findViewById<View>(R.id.change_notification).setOnClickListener { view ->
            year = calendarActual[Calendar.YEAR]
            month = calendarActual[Calendar.MONTH]
            day = calendarActual[Calendar.DAY_OF_MONTH]
            hour = calendarActual[Calendar.HOUR_OF_DAY]
            minute = calendarActual[Calendar.MINUTE]
            val datePickerDialog =
                DatePickerDialog(view.context, { view, year, monthOfYear, dayOfMonth ->
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    dateFormatted = sdfDateOnly.format(calendar.time)
                    val timePickerDialog = TimePickerDialog(view.context, { timePicker, h, m ->
                        calendar[Calendar.HOUR_OF_DAY] = h
                        calendar[Calendar.MINUTE] = m
                        calendar[Calendar.SECOND] = 0

//                                stringDateSelected = sdfDateOnly.format(calendarActual.getTime());
                        dateFormatted = dateFormatted + " " + String.format("%02d:%02d", h, m)
                        tvNotificationDateTime!!.text = "Time: $dateFormatted"
                    }, hour, minute, true)
                    timePickerDialog.setTitle(this@MainActivity.getString(R.string.select_time))
                    timePickerDialog.show()
                }, year, month, day)
            datePickerDialog.datePicker.minDate = calendarActual.timeInMillis
            datePickerDialog.show()
        }
        btnSaveNotification!!.setOnClickListener {
            val tag = generateKey()
            val alertTime = calendar.timeInMillis - System.currentTimeMillis()
            val random = (Math.random() * 50 + 1).toInt()
            val data = saveData("title", "message", random)
            saveNotification(applicationContext, alertTime, data, tag)
            Toast.makeText(this@MainActivity, "$dateFormatted Date-time", Toast.LENGTH_SHORT).show()
        }
        btnClearNotification!!.setOnClickListener { deleteNotification("tag1") }
    }

    private fun deleteNotification(tag: String) {
        WorkManager.getInstance(this).cancelAllWorkByTag(tag)
        Toast.makeText(this@MainActivity, "Alarm Clear", Toast.LENGTH_SHORT).show()
    }

    private fun generateKey(): String {
        return UUID.randomUUID().toString()
    }

    private fun saveData(title: String, message: String, id_notification: Int): Data {
        return Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .putInt("idNotification", id_notification).build()
    }
}