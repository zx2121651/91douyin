import os

file_path = 'DouyinLite/app/build.gradle.kts'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add plugins
content = content.replace('alias(libs.plugins.jetbrains.kotlin.android)', 'alias(libs.plugins.jetbrains.kotlin.android)\n    alias(libs.plugins.hilt)\n    id("kotlin-kapt")')

# 2. Add dependencies
new_deps = """
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
"""
content = content.replace('dependencies {', 'dependencies {' + new_deps)

with open(file_path, 'w') as f:
    f.write(content)
