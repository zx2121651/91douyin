import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(file_path, 'r') as f:
    lines = f.readlines()

new_lines = []
skip_next_n = 0
found_page = False
for i, line in enumerate(lines):
    if skip_next_n > 0:
        skip_next_n -= 1
        continue

    if "fun VideoPage" in line and not found_page:
        found_page = True
        new_lines.append(line)
        new_lines.append("    val hearts = remember { mutableStateListOf<LikeHeart>() }\n")
        new_lines.append("    var showCommentsSheet by remember { mutableStateOf(false) }\n")
        new_lines.append("    var showShareSheet by remember { mutableStateOf(false) }\n")
        new_lines.append("    var showLongPressMenu by remember { mutableStateOf(false) }\n")
        continue

    if "val hearts = remember { mutableStateListOf<LikeHeart>() }" in line and found_page:
        continue # skip old hearts

    if "var showCommentsSheet by remember { mutableStateOf(false) }" in line and found_page:
        continue # skip old state
    if "var showShareSheet by remember { mutableStateOf(false) }" in line and found_page:
        continue
    if "var showLongPressMenu by remember { mutableStateOf(false) }" in line and found_page:
        continue

    new_lines.append(line)

with open(file_path, 'w') as f:
    f.writelines(new_lines)
