package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AlertBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(Color(0xFFFFF5F5))
            .border(1.dp, Color(0xFFE53935))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("보스 좀비 PR 발생", fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
            Text("14일 이상 방치된 PR이 발견되었습니다.", color = Color(0xFFE53935))
        }

        Button(onClick = {}) {
            Text("전체 알림 발송")
        }
    }
}