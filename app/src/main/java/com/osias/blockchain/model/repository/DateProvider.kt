package com.osias.blockchain.model.repository

import java.util.*

//Interface usada para fazer mock da data
interface DateProvider {
    fun getDate(): Date
}