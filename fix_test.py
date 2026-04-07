import re

with open('DouyinLite/feature_home/src/test/java/com/app/douyin/pro/feature/home/domain/usecase/GetVideosUseCaseTest.kt', 'r') as f:
    content = f.read()

wrong_mock = "val mockData = listOf(\"video1\", \"video2\")"

correct_mock = """val mockData = listOf(
            com.app.douyin.pro.lib.media.network.VideoDto(1, "video1", "", 0, 0, false, "title", com.app.douyin.pro.lib.media.network.UserDto(1, "author", "", false)),
            com.app.douyin.pro.lib.media.network.VideoDto(2, "video2", "", 0, 0, false, "title", com.app.douyin.pro.lib.media.network.UserDto(1, "author", "", false))
        )"""

content = content.replace(wrong_mock, correct_mock)

with open('DouyinLite/feature_home/src/test/java/com/app/douyin/pro/feature/home/domain/usecase/GetVideosUseCaseTest.kt', 'w') as f:
    f.write(content)
