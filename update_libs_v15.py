import os

file_path = 'DouyinLite/gradle/libs.versions.toml'
with open(file_path, 'r') as f:
    content = f.read()

if 'androidx-work-runtime-ktx' not in content:
    work_libs = """
# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version = "2.9.0" }
"""
    content = content.replace('[plugins]', work_libs + '\n[plugins]')

with open(file_path, 'w') as f:
    f.write(content)
