package com.potatonetwork.happymemories

import com.getcapacitor.BridgeActivity
import android.webkit.WebView
class MainActivity : BridgeActivity() {
    override fun onStart() {
        super.onStart()

        // 안드로이드 기기의 시스템 폰트 크기 설정을 무시하고 100%로 고정
        val webView: WebView? = bridge.webView
        webView?.settings?.textZoom = 100
    }
}
