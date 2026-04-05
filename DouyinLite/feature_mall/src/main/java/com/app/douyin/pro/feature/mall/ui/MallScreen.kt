package com.app.douyin.pro.feature.mall.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.mall.ui.vm.MallViewModel

@Composable
fun MallScreen(
    onBack: () -> Unit = {},
    viewModel: MallViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF161823))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("抖音商城", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }


        if (products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF0050))
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("去购买", color = Color(0xFFFF0050), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
