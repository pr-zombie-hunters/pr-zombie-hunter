package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SummarySection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SummaryCard("전체 오픈 PR", "12", "통합 레이더 스캔 결과", Color(0xFF1565C0), "▣", Modifier.weight(1f))
        SummaryCard("새싹 좀비", "04", "방치 3-7일 경과", Color(0xFF2ECC71), "◉", Modifier.weight(1f))
        SummaryCard("좀비", "03", "방치 7-14일 경과", Color(0xFFFF9800), "◎", Modifier.weight(1f))
        SummaryCard("보스 좀비", "01", "방치 14일 초과 위험", Color(0xFFE53935), "☼", Modifier.weight(1f))
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: String,
    desc: String,
    color: Color,
    icon: String,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .height(105.dp)
            .border(1.dp, color),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = icon,
                    fontSize = 14.sp,
                    color = color
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = count,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = desc,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}