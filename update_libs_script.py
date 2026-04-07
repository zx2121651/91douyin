import re

with open('DouyinLite/gradle/libs.versions.toml', 'r') as f:
    content = f.read()

# Add versions
content = re.sub(
    r'(\[versions\]\n.*?)(\n\[libraries\])',
    r'\1\nretrofit = "2.9.0"\nokhttp = "4.12.0"\ngson = "2.10.1"\2',
    content,
    flags=re.DOTALL
)

# Add libraries
content = re.sub(
    r'(\[libraries\]\n.*?)(# Media3)',
    r'\1# Network\nretrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }\nretrofit-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }\nokhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }\nokhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }\ngson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }\n\n\2',
    content,
    flags=re.DOTALL
)

with open('DouyinLite/gradle/libs.versions.toml', 'w') as f:
    f.write(content)
