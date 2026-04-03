import os

file_path = 'DouyinLite/build.gradle.kts'
with open(file_path, 'r') as f:
    content = f.read()

# Add hilt to plugins
content = content.replace('alias(libs.plugins.android.library) apply false', 'alias(libs.plugins.android.library) apply false\n    alias(libs.plugins.hilt) apply false')
with open(file_path, 'w') as f:
    f.write(content)
