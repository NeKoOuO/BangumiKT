package com.xiaoyv.common.helper

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.EncryptUtils
import com.xiaoyv.blueprint.base.mvvm.normal.BaseViewModelActivity
import com.xiaoyv.blueprint.kts.launchUI
import com.xiaoyv.common.BuildConfig
import com.xiaoyv.common.api.BgmApiManager
import com.xiaoyv.common.api.response.GithubActionEntity
import com.xiaoyv.common.kts.CommonString
import com.xiaoyv.common.kts.debugLog
import com.xiaoyv.common.kts.openInBrowser
import com.xiaoyv.common.kts.showConfirmDialog
import com.xiaoyv.common.kts.i18n
import com.xiaoyv.widget.kts.errorMsg
import com.xiaoyv.widget.kts.showToastCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Class: [UpdateHelper]
 *
 * @author why
 * @since 12/12/23
 */
object UpdateHelper {
    const val CHANNEL_RELEASE = "Release"
    const val CHANNEL_ACTION = "Action"

    private const val TOKEN =
        "C1B418AA11A311B388C13DE2DF6C5A2FC0EE3EB9D800294A8762A8F1F3A0452E4BBF664D1DE0F13EFE29F6A8A80428E1"

    private val token by lazy {
        val key = "0123456789abcdef".toByteArray()

        EncryptUtils.decryptHexStringAES(TOKEN, key, "AES/CBC/PKCS5Padding", key)
            .decodeToString()
    }

    /**
     * Action 通道
     *
     * 更新通过对比 Tag 实现，github 发布 Release 必须严格按照 `vX.X.X` 格式创建 Tag
     */
    fun checkUpdateAction(activity: BaseViewModelActivity<*, *>, showLoading: Boolean = false) {
        activity.launchUI(
            state = if (showLoading) activity.viewModel.loadingDialogState(cancelable = false) else null,
            error = {
                it.printStackTrace()

                if (showLoading) {
                    showToastCompat(it.errorMsg)
                }
            },
            block = {
                val entity = withContext(Dispatchers.IO) {
                    BgmApiManager.bgmJsonApi.queryGithubAction(
                        name = "app-universal-release.apk",
                        pageSize = 1
                    )
                }

                val artifact = entity.artifacts?.firstOrNull()
                val headSha = artifact?.workflowRun?.headSha.orEmpty()

                require(artifact != null && headSha.isNotBlank()) { "暂无更新包发布" }
                require(BuildConfig.BUILD_HEAD_SHA != headSha) { "当前已经是最新版" }

                val downloadUrl = queryActionDownloadUrl(artifact)
                val workId = artifact.workflowRun?.id

                activity.showConfirmDialog(
                    title = i18n(CommonString.update_dialog_action_title),
                    message = i18n(CommonString.update_dialog_action_message, workId),
                    confirmText = i18n(CommonString.update_dialog_download),
                    neutralText = i18n(CommonString.update_dialog_detail),
                    cancelable = false,
                    onNeutralClick = {
                        openInBrowser("https://github.com/xiaoyvyv/bangumi/actions/runs/$workId")
                    },
                    onConfirmClick = {
                        openInBrowser(downloadUrl)
                    }
                )
            }
        )
    }

    private suspend fun queryActionDownloadUrl(artifact: GithubActionEntity.Artifact): String {
        return withContext(Dispatchers.IO) {

            debugLog { "github:$token" }
            BgmApiManager.bgmWebNoRedirectApi.queryGithubActionDownloadUrl(
                url = artifact.archiveDownloadUrl.orEmpty(),
                token = "Bearer $token"
            ).headers()["Location"].orEmpty()
        }
    }

    /**
     * Release 通道
     *
     * 更新通过对比 Tag 实现，github 发布 Release 必须严格按照 `vX.X.X` 格式创建 Tag
     */
    fun checkUpdateRelease(activity: BaseViewModelActivity<*, *>, showLoading: Boolean = false) {
        activity.launchUI(
            state = if (showLoading) activity.viewModel.loadingDialogState(cancelable = false) else null,
            error = {
                it.printStackTrace()

                if (showLoading) {
                    showToastCompat(it.errorMsg)
                }
            },
            block = {
                val entity = withContext(Dispatchers.IO) {
                    BgmApiManager.bgmJsonApi.queryGithubLatest()
                }

                val tagName = entity.tagName.orEmpty()
                require(tagName.isNotBlank()) { i18n(CommonString.update_none) }

                val versionName = AppUtils.getAppVersionName()
                val tagVersionName = tagName.removePrefix("v").trim()
                require(versionName != tagVersionName) { i18n(CommonString.update_newest) }

                val assets = entity.assets.orEmpty().firstOrNull {
                    it.contentType == "application/vnd.android.package-archive"
                            || it.browserDownloadUrl.orEmpty().endsWith(".apk", true)
                }
                requireNotNull(assets) { i18n(CommonString.update_none) }

                activity.showConfirmDialog(
                    title = i18n(CommonString.update_dialog_title),
                    message = i18n(CommonString.update_dialog_message, entity.tagName.orEmpty()),
                    confirmText = i18n(CommonString.update_dialog_download),
                    neutralText = i18n(CommonString.update_dialog_detail),
                    cancelable = false,
                    onNeutralClick = {
                        openInBrowser(entity.htmlUrl.orEmpty())
                    },
                    onConfirmClick = {
                        openInBrowser(assets.browserDownloadUrl.orEmpty())
                    }
                )
            }
        )
    }
}