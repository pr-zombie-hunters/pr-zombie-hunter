package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar() {
    Column(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .background(Color(0xFFEEF2F6))
            .padding(20.dp)
    ) {
        Text("PR 좀비 헌터", fontWeight = FontWeight.Bold, color = Color(0xFF004B8D))

        Spacer(Modifier.height(30.dp))

        Text("대시보드", fontWeight = FontWeight.Bold, color = Color(0xFF004B8D))
        Spacer(Modifier.height(16.dp))
        Text("좀비 리스트", color = Color.DarkGray)
        Spacer(Modifier.height(16.dp))
        Text("등급 규칙", color = Color.DarkGray)
    }
}