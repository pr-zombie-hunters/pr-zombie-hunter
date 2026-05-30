package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PRListSection() {
    Column {
        Text("활성 좀비 PR 목록", fontWeight = FontWeight.Bold, color = Color(0xFF004B8D))

        Spacer(Modifier.height(14.dp))

        PRListItem("보스 좀비", "feat/auth-refactor", "hunter_alpha", "D+14", "치명적 위험", Color(0xFFE53935), 0.95f, "리뷰로 처리")
        Spacer(Modifier.height(12.dp))
        PRListItem("좀비", "fix/ui-glitch-on-mobile", "dev_kyle", "D+09", "바이러스 대응중", Color(0xFFFF9800), 0.65f, "치료하기")
        Spacer(Modifier.height(12.dp))
        PRListItem("새싹 좀비", "chore/cleanup-deps", "newbie_j", "D+04", "감염 감지", Color(0xFF2ECC71), 0.25f, "바이러스 방어")
    }
}

@Composable
fun PRListItem(
    label: String,
    title: String,
    author: String,
    day: String,
    status: String,
    color: Color,
    progress: Float,
    button: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(Color.White)
            .border(1.dp, Color(0xFFD9DEE5))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("$label   $title", fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(6.dp))
            Text("생성자: @$author", color = Color.DarkGray)

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(8.dp),
                color = color
            )
        }

        Column(
            modifier = Modifier.width(140.dp)
        ) {
            Text(day, color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text(status, color = color, fontSize = 18.sp)

            Spacer(Modifier.height(6.dp))

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(button, maxLines = 1, fontSize = 12.sp)
            }
        }
    }
}