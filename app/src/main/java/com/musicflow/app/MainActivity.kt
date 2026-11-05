package com.musicflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.musicflow.app.ui.MusicFlowRoot
import com.musicflow.app.ui.theme.BgDeep
import com.musicflow.app.ui.theme.MusicFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicFlowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
                    Box(Modifier.fillMaxSize()) {
                        MusicFlowRoot()
                    }
                }
            }
        }
    }
}
