sed -i '/import androidx.compose.foundation.pager.VerticalPager/a \
import androidx.compose.foundation.pager.HorizontalPager\
import androidx.compose.foundation.clickable\
import androidx.compose.animation.core.animateFloatAsState\
import androidx.compose.animation.core.tween\
import androidx.compose.runtime.rememberCoroutineScope\
import kotlinx.coroutines.launch' DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt
