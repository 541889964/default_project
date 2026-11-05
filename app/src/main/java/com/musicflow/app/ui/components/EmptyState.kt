package com.musicflow.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicflow.app.ui.theme.TextSecondary

@Composable
fun EmptyState(title: String, subtitle: String = "") {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.MusicNote, contentDescription = null,
            modifier = Modifier.size(56.dp), tint = TextSecondary)
        Text(title, color = TextSecondary, fontSize = 15.sp,
            modifier = Modifier.padding(top = 14.dp), textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Text(subtitle, color = TextSecondary.copy(alpha = 0.7f), fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp), textAlign = TextAlign.Center)
        }
    }
}
