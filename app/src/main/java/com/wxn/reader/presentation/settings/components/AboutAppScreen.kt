package com.wxn.reader.presentation.settings.components

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.mikepenz.markdown.m3.Markdown
import com.wxn.base.ext.goShop
import com.wxn.base.util.Logger
import com.wxn.reader.R
import com.wxn.reader.data.model.AppTheme
import com.wxn.reader.domain.use_case.update.CheckAppUpdateUseCase
import com.wxn.reader.navigation.LocalNavController
import com.wxn.reader.presentation.settings.SetListItem
import com.wxn.reader.presentation.settings.viewmodels.AboutViewModel
import com.wxn.reader.presentation.settings.viewmodels.ThemeViewModel
import com.wxn.reader.util.ApkUpdateDownloader
import com.wxn.reader.util.getAppVersion
import com.wxn.reader.util.customMarkdownTypography
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    viewModel: AboutViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val themePreferences by themeViewModel.themePreferences.collectAsStateWithLifecycle()
    val isDarkTheme = when (themePreferences?.appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        else -> isSystemInDarkTheme()
    }

    val appVersion = remember { getAppVersion() }
    val reviewManager = remember { ReviewManagerFactory.create(context) }
    var requesting by remember { mutableStateOf(false) }

    fun readPrivacyPolicy(context: Context): String {
        return try {
            context.assets.open("documentation/PRIVACY_POLICY.md").bufferedReader()
                .use { it.readText() }
        } catch (e: IOException) {
            "Error loading privacy policy"
        }
    }
    val privacyPolicy = remember { readPrivacyPolicy(context) }

    var showPrivacyPolicyModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.about)) },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                // 大图标头部：对齐"我的"页（HomeMinePanel）布局
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-12).dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = stringResource(R.string.app_logo_content_desc),
                            modifier = Modifier.size(150.dp)
                        )
                        Text(
                            text = stringResource(context.applicationInfo.labelRes),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.offset(y = (-28).dp)
                        )
                        Text(
                            text = "v${appVersion?.versionName ?: "Unknown"}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.offset(y = (-28).dp)
                        )
                    }
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                SetListItem(
                    isDarkTheme = isDarkTheme,
                    text = if (updateState is AboutViewModel.UpdateUiState.Checking) {
                        stringResource(R.string.update_checking)
                    } else {
                        stringResource(R.string.settings_check_update)
                    },
                    icon = Icons.Outlined.Update
                ) {
                    viewModel.checkForUpdate()
                }
            }

            item {
                SetListItem(
                    isDarkTheme = isDarkTheme,
                    text = stringResource(R.string.rate_the_app),
                    icon = Icons.Outlined.StarRate
                ) {
                    if (requesting) return@SetListItem
                    requesting = true
                    // 手动点击过评价入口 → 永久禁用自动好评弹窗（熔断）
                    viewModel.onManualReviewClicked()
                    val request = reviewManager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val flow = reviewManager.launchReviewFlow(
                                context as ComponentActivity,
                                task.result
                            )
                            flow.addOnCompleteListener { _ ->
                                requesting = false
                            }
                        } else {
                            @ReviewErrorCode val reviewErrorCode =
                                (task.exception as? ReviewException?)?.errorCode
                            Logger.e("AboutApp Review:Error code: $reviewErrorCode")
                            // 失败兜底：打开商店详情页，保证用户有出口
                            context.goShop()
                            requesting = false
                        }
                    }
                }
            }

            item {
                SetListItem(
                    isDarkTheme = isDarkTheme,
                    text = stringResource(R.string.privacy_policy),
                    icon = Icons.Outlined.PrivacyTip
                ) {
                    showPrivacyPolicyModal = true
                }
            }
        }
    }

    // 有更新弹窗：声明式渲染（状态驱动，旋转/重组不丢失）
    (updateState as? AboutViewModel.UpdateUiState.Available)?.let { available ->
        UpdateAvailableDialog(
            result = available.result,
            onPlay = { context.goShop() },
            onDismiss = { viewModel.dismissAvailable() },
            onDownload = {
                available.result.downloadUrl?.let {
                    ApkUpdateDownloader.download(context, it, available.result.latestVersionName)
                    Toast.makeText(context, R.string.update_download_started, Toast.LENGTH_LONG).show()
                }
                viewModel.dismissAvailable()   // 发起即关弹窗
            },
        )
    }

    if (showPrivacyPolicyModal) {
        ModalBottomSheet(
            shape = BottomSheetDefaults.HiddenShape,
            dragHandle = null,
            onDismissRequest = { showPrivacyPolicyModal = false },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { it != SheetValue.PartiallyExpanded }
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Markdown(
                        typography = customMarkdownTypography(),
                        content = privacyPolicy
                    )
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showPrivacyPolicyModal = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(text = stringResource(R.string.close))
                }
            }
        }
    }

    // Toast 类一次性副作用必须由 LaunchedEffect 承载，禁止写在组合期；
    // 展示后立即 consumeNotice() 收敛状态，旋转重建也不会二次弹出
    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is AboutViewModel.UpdateUiState.Notice -> {
                Toast.makeText(context, s.messageRes, Toast.LENGTH_SHORT).show()
                viewModel.consumeNotice()
            }
            AboutViewModel.UpdateUiState.UnsupportedAbi -> {
                Toast.makeText(context, R.string.update_unsupported_abi, Toast.LENGTH_LONG).show()
                viewModel.consumeNotice()
            }
            else -> Unit
        }
    }
}

/** "新版本可用"弹窗：Play 渠道确认键跳商店，非 Play 渠道确认键发起下载。 */
@Composable
private fun UpdateAvailableDialog(
    result: CheckAppUpdateUseCase.UpdateResult.Available,
    onPlay: () -> Unit,
    onDownload: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.update_available_title)) },
        text = {
            Column {
                Text(text = "v${result.latestVersionName}")
                result.resolvedNotes?.let { notes ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = notes)
                }
            }
        },
        confirmButton = {
            if (result.downloadUrl == null) {
                TextButton(onClick = onPlay) {
                    Text(text = stringResource(R.string.update_btn_play))
                }
            } else {
                TextButton(onClick = onDownload) {
                    Text(text = stringResource(R.string.update_btn_download))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
