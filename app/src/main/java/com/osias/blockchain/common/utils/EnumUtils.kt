package com.osias.blockchain.common.utils

import com.google.gson.annotations.SerializedName


class EnumUtils {

    companion object {
        @JvmStatic fun <E : Enum<*>> getSerializedNameValue(e: E): String? {
            var value: String? = null
            try {
                value = e.javaClass.getField(e.name).getAnnotation(SerializedName::class.java)?.value
            } catch (exception: NoSuchFieldException) {
                exception.printStackTrace()
            }

            return value
        }
    }
}