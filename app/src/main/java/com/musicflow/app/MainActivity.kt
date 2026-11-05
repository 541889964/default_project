package com.musicflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.musicflow.app.ui.MusicFlowRoot
import com.musicflow.app.ui.theme.BgDeep
import com.musicflow.app.ui.theme.MusicFlowTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNeededPermissions()
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

    /** 按系统版本申请本地音频读取 + 下载写入 + 通知权限 */
    private fun requestNeededPermissions() {
        val wanted = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wanted.add(Manifest.permission.READ_MEDIA_AUDIO)
            wanted.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            wanted.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            wanted.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val toAsk = wanted.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toAsk.isNotEmpty()) permissionLauncher.launch(toAsk.toTypedArray())
    }
}