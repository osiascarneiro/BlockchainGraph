package com.osias.blockchain.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.osias.blockchain.R
import com.osias.blockchain.view.fragment.BaseFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, BaseFragment.newInstance())
//                .commitNow()
        }
    }

}
