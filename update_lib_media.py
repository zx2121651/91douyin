import re

with open('DouyinLite/lib_media/build.gradle.kts', 'r') as f:
    content = f.read()

# Add dependencies to lib_media
dependencies_block = """
    api(libs.retrofit)
    api(libs.retrofit.gson)
    api(libs.okhttp)
    api(libs.okhttp.logging)
    api(libs.gson)
"""

content = content.replace("implementation(libs.androidx.core.ktx)", "implementation(libs.androidx.core.ktx)\n" + dependencies_block)

with open('DouyinLite/lib_media/build.gradle.kts', 'w') as f:
    f.write(content)
