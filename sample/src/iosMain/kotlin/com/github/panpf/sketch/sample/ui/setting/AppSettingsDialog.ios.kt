package com.github.panpf.sketch.sample.ui.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.sample.AppSettings

@Composable
actual fun getSettingsDialogHeight(): Dp {
    return 600.dp   // TODO
}

actual fun platformMakeDecodeMenuList(appSettings: AppSettings): List<SettingItem> = emptyList()

actual fun makeZoomMenuList(appSettings: AppSettings): List<SettingItem> = emptyList()