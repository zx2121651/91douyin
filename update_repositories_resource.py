import os

def update_file(path, old, new):
    if os.path.exists(path):
        with open(path, 'r') as f:
            content = f.read()
        if old in content:
            with open(path, 'w') as f:
                f.write(content.replace(old, new))

# 1. Update HomeRepository
update_file(
    'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/data/HomeRepository.kt',
    'import com.app.douyin.pro.feature.home.data.source.HomeDataSource',
    'import com.app.douyin.pro.feature.home.data.source.HomeDataSource\nimport com.app.douyin.pro.lib.media.model.Resource'
)
update_file(
    'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/data/HomeRepository.kt',
    'suspend fun getInitialVideos(): List<String> = remoteDataSource.getVideos(0)',
    'suspend fun getInitialVideos(): Resource<List<String>> = try { Resource.Success(remoteDataSource.getVideos(0)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error") }'
)
update_file(
    'DouyinLite/feature_home/src/main/java/com/app/douyin/pro/feature/home/data/HomeRepository.kt',
    'suspend fun loadMoreVideos(page: Int): List<String> = remoteDataSource.getVideos(page)',
    'suspend fun loadMoreVideos(page: Int): Resource<List<String>> = try { Resource.Success(remoteDataSource.getVideos(page)) } catch (e: Exception) { Resource.Error(e.message ?: "Unknown Error") }'
)

# 2. Update InboxRepository
update_file(
    'DouyinLite/feature_inbox/src/main/java/com/app/douyin/pro/feature/inbox/data/InboxRepository.kt',
    'import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource',
    'import com.app.douyin.pro.feature.inbox.data.source.InboxDataSource\nimport com.app.douyin.pro.lib.media.model.Resource'
)
update_file(
    'DouyinLite/feature_inbox/src/main/java/com/app/douyin/pro/feature/inbox/data/InboxRepository.kt',
    'fun getMessages() = dataSource.getMessages()',
    'fun getMessages(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.Message>> = Resource.Success(dataSource.getMessages())'
)
update_file(
    'DouyinLite/feature_inbox/src/main/java/com/app/douyin/pro/feature/inbox/data/InboxRepository.kt',
    'fun getCategories() = dataSource.getCategories()',
    'fun getCategories(): Resource<List<com.app.douyin.pro.feature.inbox.domain.model.NotificationCategory>> = Resource.Success(dataSource.getCategories())'
)

# 3. Update MallRepository
update_file(
    'DouyinLite/feature_mall/src/main/java/com/app/douyin/pro/feature/mall/data/MallRepository.kt',
    'import com.app.douyin.pro.feature.mall.data.source.MallDataSource',
    'import com.app.douyin.pro.feature.mall.data.source.MallDataSource\nimport com.app.douyin.pro.lib.media.model.Resource'
)
update_file(
    'DouyinLite/feature_mall/src/main/java/com/app/douyin/pro/feature/mall/data/MallRepository.kt',
    'fun getMallProducts() = dataSource.getProducts()',
    'fun getMallProducts(): Resource<List<com.app.douyin.pro.feature.mall.domain.model.Product>> = Resource.Success(dataSource.getProducts())'
)
