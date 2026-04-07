package com.app.douyin.pro.feature.home.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class HeartAnimationState(
    val id: Long,
    val offset: Offset,
    val rotation: Float
)

@Composable
fun DoubleTapHeartAnimation(
    modifier: Modifier = Modifier,
    heartStates: List<HeartAnimationState>,
    onAnimationEnd: (Long) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        heartStates.forEach { state ->
            SingleHeart(
                state = state,
                onAnimationEnd = { onAnimationEnd(state.id) }
            )
        }
    }
}

@Composable
fun SingleHeart(
    state: HeartAnimationState,
    onAnimationEnd: () -> Unit
) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(state.id) {
        anim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
        onAnimationEnd()
    }

    // Scale logic: quickly bump to 1.2, then slowly scale down to 0.8
    val scale = if (anim.value < 0.2f) {
        0f + (anim.value / 0.2f) * 1.2f
    } else {
        1.2f - ((anim.value - 0.2f) / 0.8f) * 0.4f
    }

    // Alpha logic: opaque until halfway, then fade out
    val alpha = if (anim.value < 0.5f) {
        1f
    } else {
        1f - ((anim.value - 0.5f) / 0.5f)
    }

    // Offset logic: move upwards by up to 200 pixels
    val yOffset = anim.value * -200f

    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = null,
        tint = Color(0xFFFF2C55),
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (state.offset.x - 40.dp.toPx() / 2).roundToInt(),
                    y = (state.offset.y + yOffset - 40.dp.toPx() / 2).roundToInt()
                )
            }
            .size(80.dp)
            .scale(scale)
            .alpha(alpha)
            .rotate(state.rotation)
    )
}
