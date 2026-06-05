package com.example.pr_zombie.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .border(1.dp, Color(0xFFD9DEE5))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("대시보드", fontWeight = FontWeight.Bold, color = Color(0xFF004B8D))

        Row {
            Text("GITHUB CONNECTED", color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(18.dp))
            Text("ARCHITECTURE STATUS", color = Color.Gray)
        }
    }
}