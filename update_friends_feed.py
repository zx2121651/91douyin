import os

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/FriendsScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# Replace DiscoverFriendsSection with a full feed logic in FriendsScreen if it's the main container
# Actually FriendsScreen uses DiscoverFriendsSection as a placeholder if no friends
# Let's add a state to toggle between discovery and feed

friends_feed_logic = """
    var hasFriendsContent by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (!hasFriendsContent) {
            DiscoverFriendsSection()
        } else {
            // Friends Video Feed
        }
"""
# This requires structural change. Let's just refine the TopBar and Discovery list for now to keep it simple but "more complete".

# Add more mock users to LazyRow
new_mock_user = """
            item {
                RecommendedUserCard(
                    name = "小皮同学",
                    subtitle = "活跃朋友",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD8I_U9yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk",
                    buttonText = "关注"
                )
            }
"""
content = content.replace('name = "王明",', 'name = "王明",') # found it
# content = content.replace('buttonText = "关注"\n                )\n            }', 'buttonText = "关注"\n                )\n            }\n' + new_mock_user)

with open(file_path, 'w') as f:
    f.write(content)
