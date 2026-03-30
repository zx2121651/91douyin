import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    lines = f.readlines()

new_lines = []
imports = set()
for line in lines:
    if line.startswith('import '):
        if line not in imports:
            imports.add(line)
            new_lines.append(line)
    else:
        new_lines.append(line)

with open(file_path, 'w') as f:
    f.writelines(new_lines)
