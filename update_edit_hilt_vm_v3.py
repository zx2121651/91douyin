import os

file_path = 'DouyinLite/feature_edit/src/main/java/com/app/douyin/pro/feature/edit/ui/vm/EditViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Update initProject to use real metadata if possible (via helper method called from UI or internally)
# Let's keep the signature but expect duration to be passed correctly from UI which uses MediaMetadataUtils

content = content.replace('import android.net.Uri', 'import android.net.Uri\nimport androidx.lifecycle.viewModelScope\nimport kotlinx.coroutines.launch')

with open(file_path, 'w') as f:
    f.write(content)
