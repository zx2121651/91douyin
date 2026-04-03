package com.app.douyin.pro.feature.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ActionPanel(
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(bottom = 120.dp, end = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Profile Picture with Follow Button
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

            AnimatedVisibility(
                visible = !isFollowed,
                \x65xit = fadeOut() + scaleOut(),
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
        }

        LikeButton()
        CommentButton(onClick = onCommentClick)
        FavoriteButton()
        ShareButton(onClick = onShareClick)

        // Spinning Record Placeholder
        Box(modifier = Modifier.size(48.dp).background(Color.DarkGray, CircleShape))
    }
}

@Composable
fun LikeButton() {
    var isLiked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (isLiked) 1.2f else 1.0f, label = "likeScale")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = "Like",
            tint = if (isLiked) Color(0xFFFF2C55) else Color.White,
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isLiked = !isLiked }
                )
        )
        Text(text = "12.5w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CommentButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.AddCircle, // Placeholder for actual icon
            contentDescription = "Comment",
            tint = Color.White,
            modifier = Modifier.size(40.dp).clickable { onClick() }
        )
        Text(text = "3.2w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FavoriteButton() {
    var isFavorited by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Favorite",
            tint = if (isFavorited) Color(0xFFFFD700) else Color.White,
            modifier = Modifier.size(40.dp).clickable { isFavorited = !isFavorited }
        )
        Text(text = "2.1w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShareButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share",
            tint = Color.White,
            modifier = Modifier.size(40.dp).clickable { onClick() }
        )
        Text(text = "8.5w", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
