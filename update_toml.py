import os

file_path = 'DouyinLite/gradle/libs.versions.toml'
with open(file_path, 'r') as f:
    content = f.read()

# Add hilt version
content = content.replace('camerax = "1.3.3"', 'camerax = "1.3.3"\nhilt = "2.51.1"')

# Add hilt libraries
hilt_libs = """
# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
"""
content = content.replace('[plugins]', hilt_libs + '\n[plugins]')

# Add hilt plugin
hilt_plugin = 'hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }'
content = content + hilt_plugin + "\n"

with open(file_path, 'w') as f:
    f.write(content)
