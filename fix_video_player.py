import sys

file_path = "DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt"
with open(file_path, 'r') as f:
    content = f.read()

search_str = """    LaunchedEffect(isVisible) {
        if (isVisible) {
            player.play()
        } else {
            player.pause()
        }
    }

    DisposableEffect(url) {
        onDispose {
            playerManager.releasePlayer(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                androidx.media3.ui.PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Progress computation"""

replace_str = """    var isVideoReady by remember { mutableStateOf(false) }

    // Listener for video ready state
    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_READY) {
                    isVideoReady = true
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            player.play()
        } else {
            player.pause()
            player.seekTo(0) // Precise lifecycle hook: Reset to 0 when unselected
        }
    }

    DisposableEffect(url) {
        onDispose {
            playerManager.releasePlayer(url)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                // Using SurfaceView implicitly via PlayerView, optimal for power consumption
                androidx.media3.ui.PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    // Ensure we use SurfaceView (default behavior of PlayerView if not overridden in XML)
                    // If XML specifies textureView, programmatically setting it here is more complex without inflation.
                    // By default, PlayerView inflates exo_player_view.xml which uses a SurfaceView.
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Placeholder and Alpha Animation
        // Since we don't have real thumbnails in MockData, we generate a synthetic one or use a dark placeholder
        // Using an AnimatedVisibility or animateFloatAsState for the alpha transition (300ms)
        val placeholderAlpha by animateFloatAsState(
            targetValue = if (isVideoReady) 0f else 1f,
            animationSpec = tween(durationMillis = 300),
            label = "placeholderAlpha"
        )

        if (placeholderAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF161823).copy(alpha = placeholderAlpha)) // Douyin dark theme surface color
            ) {
                // Here we would use coil AsyncImage if we had a thumbnail URL:
                // AsyncImage(model = thumbnailUrl, contentDescription = null, contentScale = ContentScale.Crop)
            }
        }

        // Progress computation"""

if search_str in content:
    content = content.replace(search_str, replace_str)
    with open(file_path, 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Could not find replacement string")
