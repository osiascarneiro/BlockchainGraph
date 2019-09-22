package com.osias.blockchain.viewmodel

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.GsonBuilder
import com.osias.blockchain.model.local.BancoLocal
import com.osias.blockchain.model.remote.Service
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type

open class BaseViewModelTests {

    @Rule
    @JvmField
    var rule = InstantTaskExecutorRule()

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var localDb: BancoLocal

    @Mock
    lateinit var blockchainService: Service

    private lateinit var erroDefault: Throwable

    @Before
    open fun setUp() {
        MockitoAnnotations.initMocks(this)
        erroDefault = Throwable("Erro")
    }

    @Test
    fun nothing() {
        assert(true)
    }

    fun <T> getObject(fileName: String, type: Type) : T? {
        val inputStream = this.javaClass.classLoader?.getResourceAsStream("$fileName.json")

        val r = BufferedReader(InputStreamReader(inputStream))
        val json = StringBuilder()
        for (line in r.lines()) {
            json.append(line)
        }

        val jsonString = json.toString()
        val gson = GsonBuilder().create()
        return gson.fromJson<T>(jsonString, type)
    }

}