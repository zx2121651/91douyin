import sys
file_path = sys.argv[1]
with open(file_path, 'r') as f:
    content = f.read()

# Add plugins
content = content.replace('alias(libs.plugins.jetbrains.kotlin.android)', 'alias(libs.plugins.jetbrains.kotlin.android)\n    alias(libs.plugins.hilt)\n    id("kotlin-kapt")')

# Add dependencies
new_deps = """
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
"""
content = content.replace('dependencies {', 'dependencies {' + new_deps)

with open(file_path, 'w') as f:
    f.write(content)
