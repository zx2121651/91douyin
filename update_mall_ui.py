import os

file_path = 'DouyinLite/feature_mall/src/main/java/com/app/douyin/pro/feature/mall/ui/MallScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('background(Color(0xFFF8F8F8))', 'background(Color(0xFF161823))')
content = content.replace('Text("商城", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)', 'Text("抖音商城", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)')
content = content.replace('Card(shape = RoundedCornerShape(12.dp))', 'Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E2E)))')
content = content.replace('Text(item, style = MaterialTheme.typography.titleMedium)', 'Text(item, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)')
content = content.replace('Icon(Icons.Filled.ArrowBack, contentDescription = "Back")', 'Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)')

# Add import for sp
if 'import androidx.compose.ui.unit.sp' not in content:
    content = content.replace('import androidx.compose.ui.unit.dp', 'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp')
if 'CardDefaults' not in content:
    content = content.replace('import androidx.compose.material3.Card', 'import androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults')

with open(file_path, 'w') as f:
    f.write(content)
