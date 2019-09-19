package com.osias.blockchain.common.utils

import java.util.*

class DateUtil {

    companion object {

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