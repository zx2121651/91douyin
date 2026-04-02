import os

home_path = 'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/ui/HomeScreen.kt'
with open(home_path, 'r') as f:
    content = f.read()

# 1. Update HomeScreen signature
content = content.replace(
    'fun HomeScreen(onNavigateToMall: () -> Unit = {}) {',
    'fun HomeScreen(onNavigateToMall: () -> Unit = {}, onNavigateToProfile: () -> Unit = {}) {'
)

# 2. Add gesture detector to recommendation Pager
old_pager = """                    VerticalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { vPage ->"""

new_pager = """                    VerticalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 1,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { change, dragAmount ->
                                    if (dragAmount < -30f) { // Left swipe
                                        onNavigateToProfile()
                                    }
                                }
                            }
                    ) { vPage ->"""

content = content.replace(old_pager, new_pager)
with open(home_path, 'w') as f:
    f.write(content)

# 3. Update MainActivity call
main_path = 'DouyinLite/app/src/main/java/com/app/douyin/pro/MainActivity.kt'
with open(main_path, 'r') as f:
    main_content = f.read()

main_content = main_content.replace(
    'HomeScreen(onNavigateToMall = { navController.navigate("mall_standalone") })',
    'HomeScreen(onNavigateToMall = { navController.navigate("mall_standalone") }, onNavigateToProfile = { navController.navigate(BottomNavItem.Me.route) })'
)
with open(main_path, 'w') as f:
    f.write(main_content)
