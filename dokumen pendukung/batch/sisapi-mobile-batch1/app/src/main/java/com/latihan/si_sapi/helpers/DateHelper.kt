package com.latihan.si_sapi.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Format yang diminta Laravel untuk rule `date`: yyyy-MM-dd */
fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
