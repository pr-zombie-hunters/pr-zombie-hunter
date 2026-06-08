package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RuleBox() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0))
            .padding(18.dp)
    ) {
        Text(
            text = "방치 기간 및 위험 분류 기준",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RuleItem(
                title = "🛡️ 0~2일 정상",
                desc = "코드 싱싱도가 높은 안전 상태",
                color = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )

            RuleItem(
                title = "🌱 3~6일 새싹 좀비",
                desc = "관심이 필요한 초기 감염 상태",
                color = Color(0xFF2ECC71),
                modifier = Modifier.weight(1f)
            )

            RuleItem(
                title = "☣️ 7~13일 좀비",
                desc = "안정도를 저해하는 바이러스 상태",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            RuleItem(
                title = "💀 14일+ 보스 좀비",
                desc = "시스템 붕괴를 초래하는 치명적 위험",
                color = Color(0xFFE53935),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RuleItem(
    title: String,
    desc: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(82.dp)
            .border(1.dp, color)
            .background(Color(0xFFFAFAFA))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = desc,
            fontSize = 15.sp,
            color = Color(0xFF555555),
            lineHeight = 14.sp
        )
    }
}