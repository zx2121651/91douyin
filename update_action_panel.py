import re

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/components/ActionPanel.kt', 'r') as f:
    content = f.read()

# Update signature to accept avatarUrl
content = content.replace("    modifier: Modifier = Modifier\n) {", "    modifier: Modifier = Modifier,\n    avatarUrl: String = \"\"\n) {")

# Update painter
content = content.replace("""painter = rememberAsyncImagePainter("https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk")""", "painter = rememberAsyncImagePainter(if (avatarUrl.isNotEmpty()) avatarUrl else \"https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk\")")

with open('DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/components/ActionPanel.kt', 'w') as f:
    f.write(content)
