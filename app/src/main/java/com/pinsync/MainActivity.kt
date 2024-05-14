package com.pinsync

import androidx.compose.runtime.Composable
import com.pinsync.ui.PinSyncApp

class MainActivity : PinSyncActivity() {
    @Composable
    override fun ActivityBody() {
        PinSyncApp(viewModel = viewModel)
    }
}
