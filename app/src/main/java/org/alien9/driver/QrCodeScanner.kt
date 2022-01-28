package org.alien9.driver

import android.content.Intent
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result

class QrCodeScanner : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        val intent = Intent()
        intent.putExtra("CODE", rawResult.text)
        setResult(RESULT_OK, intent)
        finish()
    }
}