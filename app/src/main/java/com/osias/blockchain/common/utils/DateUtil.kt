package com.osias.blockchain.common.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    companion object {

        fun formatDate(date: Date, withTime: Boolean = true): String {
            val format = if(withTime) "dd/MM/yyyy HH:mm" else "dd/MM/yyyy"
            return SimpleDateFormat(format, Locale.getDefault()).format(date)
        }

        fun stripMinutes(from: Date): Date {
            Calendar.getInstance().apply {
                timeInMillis = from.time
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                return time
            }
        }

    }

}