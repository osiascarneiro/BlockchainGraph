package com.osias.blockchain.common.converter

import androidx.room.TypeConverter
import com.osias.blockchain.model.enumeration.ChartPeriod

class ChartPeriodConverter {

        @TypeConverter
        fun toChartPeriod(name: String): ChartPeriod {
            return ChartPeriod.valueOf(name)
        }

        @TypeConverter
        fun fromChartPeriod(period: ChartPeriod): String {
            return period.name
        }

}