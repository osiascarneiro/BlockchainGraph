package com.osias.blockchain.model.remote

import com.osias.blockchain.common.utils.EnumUtils.Companion.getSerializedNameValue
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type


class EnumRetrofitConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, String>? {
        if (type is Class<*> && type.isEnum) {
            return Converter<Any?, String> { value -> getSerializedNameValue(value as Enum<*>) }
        }
        return null
    }
}