package com.osias.blockchain.common.converter

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
        @TypeConverter
        fun toDate(time: Long?): Date? {
            time?.let { return Date(it) }
            return null
        }

        @TypeConverter
        fun fromDate(date: Date?): Long? {
            date?.let {
                return it.time
            }
            return null
        }
}