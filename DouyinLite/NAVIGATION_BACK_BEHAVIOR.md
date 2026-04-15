# 导航回退行为说明文档 (Navigation Back Behavior Documentation)

## 概述
本项目采用 Compose Navigation 构建多 Tab 架构，实现了符合抖音用户习惯的导航回退行为。

## 一级导航 (Main Tabs)
一级导航包含：首页、朋友、拍摄、消息、我。

### 回退行为规则
1. **状态保留**：切换一级 Tab 时，使用 `saveState = true` 和 `restoreState = true`。这意味着用户从“首页”切换到“消息”，再切回“首页”时，首页的滚动位置和播放状态将被保留。
2. **栈顶管理**：导航至一级 Tab 时使用 `launchSingleTop = true`，防止在返回栈中出现重复的一级页面实例。
3. **根页面回退**：所有一级 Tab 导航都相对于导航图的起始目的地（首页）进行 `popUpTo` 操作。当用户在非“首页”的 Tab 点击系统返回键时，默认行为是返回到“首页”。在“首页”点击返回键将退出应用。

## 二级页面 (Secondary Screens)
二级页面包括：编辑页 (Edit)、聊天页 (Chat)、商城独立页 (Mall)。

### 回退行为规则
1. **普通压栈**：二级页面通过 `navController.navigate` 正常压入返回栈。
2. **返回键处理**：
   - **编辑页**：点击返回或关闭调用 `popBackStack` 返回拍摄页。点击“下一步”发布成功后，通过 `popUpTo(NavRoutes.HOME) { inclusive = true }` 清空返回栈并跳转回首页，模拟发布后的流程闭环。
   - **聊天页**：点击返回调用 `popBackStack` 返回消息列表。
   - **商城页**：作为独立容器压栈，点击返回调用 `popBackStack` 返回前一个页面。

## 底栏显隐规则 (Bottom Bar Visibility)
1. **显示**：在一级 Tab 页面（首页、朋友、消息、我）显示底栏。
2. **隐藏**：在拍摄页、编辑页、聊天页、商城页隐藏底栏，以提供沉浸式体验。
3. **实现方式**：在 `MainScreen` 中根据 `currentRoute` 结合 `NavigationConfigs.shouldHideBottomBar` 动态控制 `Scaffold` 的 `bottomBar` 渲染。

## 总结
通过集中化的 `NavRoutes` 管理路由字符串，以及 `NavigationConfigs` 统一显隐逻辑，确保了导航行为的可预测性和架构的可扩展性。
