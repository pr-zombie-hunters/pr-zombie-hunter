package com.example.pr_zombie.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pr_zombie.ui.component.*

@Composable
fun MainScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
    ) {
        Sidebar()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            TopBar()

            Spacer(Modifier.height(18.dp))
            AlertBox()

            Spacer(Modifier.height(18.dp))
            SummarySection()

            Spacer(Modifier.height(24.dp))
            PRListSection()

            Spacer(Modifier.height(22.dp))
            RuleBox()
        }
    }
}