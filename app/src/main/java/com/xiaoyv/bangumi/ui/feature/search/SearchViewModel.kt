package com.xiaoyv.bangumi.ui.feature.search

import androidx.lifecycle.MutableLiveData
import com.xiaoyv.blueprint.base.mvvm.normal.BaseViewModel
import com.xiaoyv.blueprint.kts.launchUI
import com.xiaoyv.common.config.annotation.BgmPathType
import com.xiaoyv.common.config.annotation.SearchCatType
import com.xiaoyv.common.config.bean.SearchItem
import com.xiaoyv.common.helper.CacheHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Class: [SearchViewModel]
 *
 * @author why
 * @since 12/9/23
 */
class SearchViewModel : BaseViewModel() {
    internal val currentSearchItem = MutableLiveData<SearchItem>()

    internal val onSearchSubjectLiveData = MutableLiveData<List<SearchItem>>()
    internal val onSearchPersonLiveData = MutableLiveData<List<SearchItem>>()
    internal val onSearchRecentlyLiveData = MutableLiveData<List<SearchItem>?>()

    override fun onViewCreated() {
        querySearchItems()
    }

    private fun querySearchItems() {
        launchUI {
            onSearchSubjectLiveData.value = listOf(
                SearchItem(
                    label = "全部",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_ALL,
                ),
                SearchItem(
                    label = "动画",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_ANIME,
                ),
                SearchItem(
                    label = "书籍",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_BOOK,
                ),
                SearchItem(
                    label = "音乐",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_MUSIC,
                ),
                SearchItem(
                    label = "游戏",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_GAME,
                ),
                SearchItem(
                    label = "三次元",
                    pathType = BgmPathType.TYPE_SEARCH_SUBJECT,
                    id = SearchCatType.TYPE_REAL,
                ),
            )

            onSearchPersonLiveData.value = listOf(
                SearchItem(
                    label = "全部",
                    pathType = BgmPathType.TYPE_SEARCH_MONO,
                    id = SearchCatType.TYPE_ALL,
                ),
                SearchItem(
                    label = "虚拟角色",
                    pathType = BgmPathType.TYPE_SEARCH_MONO,
                    id = SearchCatType.TYPE_CHARACTER,
                ),
                SearchItem(
                    label = "现实人物",
                    pathType = BgmPathType.TYPE_SEARCH_MONO,
                    id = SearchCatType.TYPE_PERSON,
                )
            )

            onSearchRecentlyLiveData.value = withContext(Dispatchers.IO) {
                CacheHelper.readSearchHistory()
            }

            // 默认搜索 条目-全部
            currentSearchItem.value = onSearchSubjectLiveData.value?.firstOrNull()
        }
    }
}