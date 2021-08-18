package org.alien9.driver

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class JsWebInterface(context: Context) {
    val context=context
    @JavascriptInterface
    fun makeToast(message: String?, lengthLong: Boolean) {

        Toast.makeText(
            context,
            message,
            if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }
}