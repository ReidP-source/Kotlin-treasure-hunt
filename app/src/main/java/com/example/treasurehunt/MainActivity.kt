package com.example.treasurehunt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.treasurehunt.ui.theme.TreasurehuntTheme
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreasurehuntTheme {
                TreasureHuntApp()
            }
        }
    }
}
