import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/screen/EditScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add MediaMetadataUtils import
new_imports = """
import com.app.douyin.pro.lib.media.util.MediaMetadataUtils
import androidx.compose.ui.platform.LocalContext
"""
if 'MediaMetadataUtils' not in content:
    content = content.replace('import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel', 'import com.app.douyin.pro.feature.edit.ui.vm.EditViewModel' + new_imports)

# 2. Use MediaMetadataUtils in LaunchedEffect
old_init = """    LaunchedEffect(videoUri) {
        if (videoUri.isNotEmpty()) {
            viewModel.initProject(Uri.parse(videoUri), 15000L)
        }
    }"""

new_init = """    val context = LocalContext.current
    LaunchedEffect(videoUri) {
        if (videoUri.isNotEmpty()) {
            val uri = Uri.parse(videoUri)
            val duration = MediaMetadataUtils.getVideoDurationMs(context, uri)
            viewModel.initProject(uri, if (duration > 0) duration else 15000L)
        }
    }"""

if old_init in content:
    content = content.replace(old_init, new_init)

with open(file_path, 'w') as f:
    f.write(content)
