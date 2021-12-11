package org.alien9.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.webkit.WebViewAssetLoader
import java.util.*

class LocalContentWebClient : WebViewClient {

    constructor(a: WebViewAssetLoader, backend: String){
        this.assetLoader=a
        this.backend =backend
    }
    var backend: String = ""
    var assetLoader: WebViewAssetLoader

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return request?.let {
            var intercepted = this.assetLoader.shouldInterceptRequest(request.url)
            if(request.url.toString().endsWith("js")){
                if(intercepted!=null){
                    intercepted.mimeType="text/javascript"
                }
            }
            return intercepted
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        Log.d("DRIVER", "page was loaded")
        view.evaluateJavascript("""
            localStorage.setItem('backend', '${backend}');
            var l=localStorage.getItem('language');
            localStorage.setItem('language', '${Locale.getDefault().language}');
            if(!l || l!='${Locale.getDefault().language}')
                window.location.reload();
            """) {}
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        super.onLoadResource(view, url)
        Log.d("DRIVER", "resource was loaded")
    }

    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        view?.loadUrl("file:///android_asset/unreachable.html")
    }

}

