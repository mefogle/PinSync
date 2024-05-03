package com.pinsync.ui

import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pinsync.api.PinApi

@Composable
fun AuthDialog(onDismissRequest: () -> Unit) {

    val initialUrl = "https://humane.center"
    @Suppress("SetJavaScriptEnabled")
    Dialog(onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // Content of the dialog
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(16.dp).clip(RoundedCornerShape(16.dp)),
            factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        if (PinApi.isAuthenticated()) {
                            Log.d("AuthDialog", "Authentication complete")
                            onDismissRequest()
                        }
                    }
                }
                loadUrl(initialUrl)
                settings.javaScriptEnabled = true // Enable JavaScript
            }
        })
    }
}


