import os

file_path = 'DouyinLite/feature_profile/src/main/java/com/app/douyin/pro/feature/profile/ui/ProfileScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

new_imports = """
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.douyin.pro.feature.profile.viewmodel.ProfileViewModel
"""
content = content.replace('import kotlin.random.Random', 'import kotlin.random.Random' + new_imports)

old_body = """@Composable
fun ProfileScreen() {
    val darkBg = Color(0xFF161823)"""

new_body = """@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val username by viewModel.username.collectAsState()
    val douyinId by viewModel.douyinId.collectAsState()
    val darkBg = Color(0xFF161823)"""

content = content.replace(old_body, new_body)

# Replace hardcoded values
content = content.replace('text = "南京最帅程序员",', 'text = username,')
content = content.replace('text = "抖音号: JulesCode_99",', 'text = "抖音号: ",')

with open(file_path, 'w') as f:
    f.write(content)
