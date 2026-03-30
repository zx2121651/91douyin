sed -i '/import androidx.compose.runtime.snapshotFlow/a \
import androidx.media3.common.Player\
import androidx.compose.ui.layout.ContentScale\
import androidx.compose.animation.core.tween\
import androidx.compose.animation.core.animateFloatAsState\
import coil.compose.AsyncImage' DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt
