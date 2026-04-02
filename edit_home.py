import sys

file_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'

with open(file_path, 'r') as f:
    lines = f.readlines()

content = "".join(lines)

# 1. Update RightSideActions to support Share click
content = content.replace(
    'fun RightSideActions(onCommentClick: () -> Unit, modifier: Modifier = Modifier) {',
    'fun RightSideActions(onCommentClick: () -> Unit, onShareClick: () -> Unit, modifier: Modifier = Modifier) {'
)

# 2. Update the call to ShareButton in RightSideActions
content = content.replace(
    '        ShareButton()',
    '        ShareButton(onClick = onShareClick)'
)

# 3. Add Follow button animation logic
old_profile_box = """        // Profile Picture with Follow Button
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 8.dp)) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )
            Icon(
                imageVector = Icons.Filled.AddCircle,
                contentDescription = "Follow",
                tint = Color(0xFFFF2C55), // Douyin Red
                modifier = Modifier
                    .size(20.dp)
                    .offset(y = 10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }"""

new_profile_box = """        // Profile Picture with Follow Button
        var isFollowed by remember { mutableStateOf(false) }
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.padding(bottom = 8.dp)) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAFRJnvPgLJTZNlp2beH3rKkgrIq79yAByHrNztp31d3S5Ql5HDcVsXOtOffLNhtuX4qaajnkwFgdAFL5OCuwdLzNBs9QDqqeiJejfbJPzXVeArU5eX10395R9he1IM-Eoy2kh6lmFA_v6n8auwbHfT6iBKAZdZODWoz0wWWJn57dDE7AybZhChYpQ6vVgt7ESF1A6VaNFSrjxMK6MuHftCkoxICASpEx6ooT2VDLv3mlsVbLQNXGa1uCeoOWCamXI699HkQHUvmOk",
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !isFollowed,
                \x65xit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
                modifier = Modifier.offset(y = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Follow",
                    tint = Color(0xFFFF2C55),
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { isFollowed = true }
                )
            }
        }"""

content = content.replace(old_profile_box, new_profile_box)

# 4. Update ShareButton definition
old_share_def = """fun ShareButton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )"""

new_share_def = """fun ShareButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )"""

content = content.replace(old_share_def, new_share_def)

with open(file_path, 'w') as f:
    f.write(content)
