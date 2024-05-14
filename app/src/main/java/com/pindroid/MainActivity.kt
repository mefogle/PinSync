package com.pindroid

import androidx.compose.runtime.Composable
import com.pindroid.ui.PinDroidApp

class MainActivity : PinDroidActivity() {
    @Composable
    override fun ActivityBody() {
        PinDroidApp(viewModel = viewModel)
    }
}
