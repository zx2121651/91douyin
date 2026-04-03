import os

file_path = 'DouyinLite/lib_media/build.gradle.kts'
with open(file_path, 'r') as f:
    content = f.read()

new_deps = """
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.media3.effect)
    implementation(libs.androidx.media3.transformer)
"""
if 'libs.androidx.work.runtime.ktx' not in content:
    content = content.replace('dependencies {', 'dependencies {' + new_deps)

with open(file_path, 'w') as f:
    f.write(content)
