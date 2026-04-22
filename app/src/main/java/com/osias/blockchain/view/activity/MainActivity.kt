package com.osias.blockchain.view.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.osias.blockchain.ui.navigation.AppNavGraph
import com.osias.blockchain.ui.theme.BlockchainGraphTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockchainGraphTheme {
                AppNavGraph()
            }
        }
    }

}
