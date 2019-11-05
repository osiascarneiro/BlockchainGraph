package com.osias.blockchain.common.converter

import androidx.room.TypeConverter
import com.osias.blockchain.common.utils.DateUtil
import java.util.*

class DateConverter {

    companion object {
        @TypeConverter
        @JvmStatic fun toDate(time: Long?): Date? {
            time?.let { return Date(it) }
            return null
        }

        @TypeConverter
        @JvmStatic fun fromDate(date: Date?): Long? {
            date?.let {
                return it.time
            }
            return null
        }
    }

}