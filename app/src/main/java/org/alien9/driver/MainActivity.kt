package org.alien9.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.media.ThumbnailUtils
import android.media.ThumbnailUtils.createImageThumbnail
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.webkit.*
import android.webkit.WebView.WebViewTransport
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

private const val FCR = 1
class MainActivity : AppCompatActivity() {
    private lateinit var photoURI: Uri
    private var media_uri: Uri?=null
    private var basepath: File? = null
    private var frontend: String? = null
    private val REQUEST_IMAGE_CAPTURE=1
    private var backend: String? = null
    private var state: String? = ""
     private var mCM: String? = null
 private var mUM: ValueCallback<Uri>? = null
 private var mUMA: ValueCallback<Array<Uri>>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(true);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        basepath=this.getExternalFilesDir(null)
        val PERMISSION_ALL = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS

        )

        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL)
        }


        var mUploadMessage: ValueCallback<Uri>;
        Companion.FILECHOOSER_RESULTCODE = 1;

        var loadingFinished = true
        var redirect = false
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setTitle(R.string.application_name)
        setSupportActionBar(toolbar)

        val mWebview: WebView = findViewById(R.id.webview)

        val webSettings: WebSettings = mWebview.settings
        webSettings.setSupportMultipleWindows(true)
        webSettings.domStorageEnabled=true
        webSettings.setGeolocationEnabled(true)
        webSettings.javaScriptEnabled=true
        webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically=false
        webSettings.allowFileAccess=true
        webSettings.allowFileAccessFromFileURLs=true
        webSettings.cacheMode=WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSettings.setGeolocationEnabled(true)
        mWebview.addJavascriptInterface(JsWebInterface(this, mWebview), "androidApp")
        val context=this
        mWebview.setWebViewClient(object: WebViewClient(){
            override fun onPageFinished(view: WebView, url: String) {
                Log.d("DRIVER", "page was loaded")
                mWebview.loadUrl("javascript:localStorage.setItem('backend', '%s')".format(backend))
                var locationManager: LocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                val location: Location? =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                mWebview.loadUrl("javascript:localStorage.setItem('latitude', "+location?.latitude+");localStorage.setItem('longitude', "+location?.longitude+");")

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
        })
        mWebview.setWebChromeClient(object : WebChromeClient() {
            override fun onShowFileChooser(webView:WebView, filePathCallback:ValueCallback<Array<Uri>>, fileChooserParams:FileChooserParams):Boolean {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent?.resolveActivity(packageManager)?.also {
                        }        // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        this@MainActivity.photoURI = FileProvider.getUriForFile(
                            webView.context,
                            "org.alien9.driver.fileprovider",
                            it
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);

            }
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback){
                callback.invoke(origin, true, false)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(
                    "DRIVER-CONSOLE", consoleMessage.message() + " -- From line "
                            + consoleMessage.lineNumber() + " of "
                            + consoleMessage.sourceId()
                )
                return super.onConsoleMessage(consoleMessage)
            }
            fun onPageFinished(view: WebView?, url: String?) {
                Log.d("DRIVER", "page was loaded")

            }
            override fun onCreateWindow(
                view: WebView, isDialog: Boolean,
                isUserGesture: Boolean, resultMsg: Message
            ): Boolean {
                val newWebView = WebView(this@MainActivity)
                view.addView(newWebView)
                val transport = resultMsg.obj as WebViewTransport
                transport.webView=newWebView
                resultMsg.sendToTarget()
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        url: String
                    ): Boolean {
                        val browserIntent = Intent(Intent.ACTION_VIEW)
                        browserIntent.data = Uri.parse(url)
                        startActivity(browserIntent)
                        return true
                    }
                }
                return true
            }
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d("DRIVER", "onPermissionRequest")
                runOnUiThread {
                    Log.d("DRIVER", request.origin.toString())
                    if (request.origin.toString() == "file:///") {
                        Log.d("DRIVER", "GRANTED")
                        request.grant(request.resources)
                    } else {
                        Log.d("DRIVER", "DENIED")
                        request.deny()
                    }
                }
            }

        })


        val activity=this@MainActivity
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        backend = sharedPref.getString("backend", getString(R.string.backend) )
        frontend = sharedPref.getString("frontend", getString(R.string.frontend) )

        mWebview.loadUrl("${frontend}/index.html")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }
    lateinit var currentPhotoPath: String
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val filename=java.lang.Long.toHexString(System.currentTimeMillis())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${filename}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val thumbImage: Bitmap = createImageThumbnail(
                File(currentPhotoPath),
                Size(256,256), null
            )
            var bas=ByteArrayOutputStream()
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, bas)
            var byteArray = bas.toByteArray();
            var ext=Base64.encodeToString(byteArray, Base64.DEFAULT);
            var j= JSONObject()
            j.put("src", ext)
            var u="javascript:(document.getElementById('file-name')).setAttribute('value', '${currentPhotoPath}');"
            findViewById<WebView>(R.id.webview).loadUrl(u)
            u="javascript:var t=document.getElementById('image-loader');t.setAttribute('value', '${ext}');var event = new Event('change');t.dispatchEvent(event);"
            findViewById<WebView>(R.id.webview).loadUrl(u)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val w=findViewById(R.id.webview) as WebView

        w.evaluateJavascript("(function(){return $('#image-loader').length})();") { value ->
            menu?.findItem(R.id.take_photo)?.isVisible=value=="1"
        }
        menu?.findItem(R.id.action_refresh)?.isVisible=state=="list"
        menu?.findItem(R.id.take_photo)?.isVisible=true
        menu?.findItem(R.id.action_map)?.isVisible=state=="input"
        menu?.findItem(R.id.action_logout)?.isVisible=true
        menu?.findItem(R.id.upload)?.isVisible=state=="list"
        return super.onPrepareOptionsMenu(menu)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_context_menu, menu)
        return true
    }
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val myString: String=""
        return when (item.itemId) {
            R.id.reload -> {
                val w=findViewById(R.id.webview) as WebView
                w.clearCache(true)
                w.loadUrl("javascript:localStorage.setItem('backend', '%s')".format(backend))
val a=this@MainActivity
                val sharedPref = a?.getPreferences(Context.MODE_PRIVATE)
                backend = sharedPref.getString("backend", getString(R.string.backend))
                frontend = sharedPref.getString("frontend",  getString(R.string.frontend))

                w.loadUrl("${frontend}/index.html")
                true
            }
            R.id.take_photo->{
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                takePictureIntent?.resolveActivity(packageManager)?.also {
                }        // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    this@MainActivity.photoURI = FileProvider.getUriForFile(
                        this,
                        "org.alien9.driver.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }





                true
            }
            R.id.action_settings -> {
                val builder = AlertDialog.Builder(this)
                val a=this@MainActivity
                val inflater = layoutInflater
                builder.setTitle("Backend")
                builder.setMessage("host")
                val dialogLayout = inflater.inflate(R.layout.alert_edit, null)
                val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
                val frontendText  = dialogLayout.findViewById<EditText>(R.id.frontendText)
                val sharedPref = a?.getPreferences(Context.MODE_PRIVATE)
                backend = sharedPref.getString("backend", getString(R.string.backend))
                frontend = sharedPref.getString("frontend", getString(R.string.frontend))
                editText.setText(backend)
                frontendText.setText(frontend)
                builder.setView(dialogLayout)
                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                    val sharedPref = a?.getPreferences(Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putString("backend", editText.text.toString())
                        putString("frontend", frontendText.text.toString())
                        apply()
                    }
                    (findViewById(R.id.webview) as WebView).clearCache(true)
                    (findViewById(R.id.webview) as WebView).loadUrl("javascript:localStorage.setItem('backend', '%s');localStorage.removeItem('dataset')".format(backend))
                }

                builder.setNegativeButton(android.R.string.cancel) { dialog, which ->

                }
                builder.show()
                true
            }
            R.id.action_logout -> {
                (findViewById(R.id.webview) as WebView).loadUrl("javascript:$('#list-logout-button').click();")
                true
            }
            R.id.action_refresh->{
                (findViewById(R.id.webview) as WebView).loadUrl("javascript:goto('tab','refresh');")
                true

            }
            R.id.upload->{
                (findViewById(R.id.webview) as WebView).loadUrl("javascript:goto('command','upload');")
                true
            }
            R.id.action_map->{
                var locationManager: LocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                (findViewById(R.id.webview) as WebView).loadUrl("javascript:localStorage.setItem('latitude', %.7f);localStorage.setItem('longitude', %.7f);".format(location?.latitude,location?.longitude))
                (findViewById(R.id.webview) as WebView).loadUrl("javascript:goto('tab', 'location');")

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        var FILECHOOSER_RESULTCODE = 1
    }

    inner class JsWebInterface(context: Context, w: WebView) {
        val context=context
        val webview=w
        @JavascriptInterface
        fun makeToast(message: String?, lengthLong: Boolean) {
            Toast.makeText(
                context,
                message,
                if (lengthLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
        @JavascriptInterface
        fun changeLocation(){
            var locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val s = object: Runnable{
                override fun run() {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    val location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    val u="javascript:localStorage.setItem('latitude', %.7f);localStorage.setItem('longitude', %.7f);".format(location?.latitude,location?.longitude)
                    webview.loadUrl(u);
//                    webview.loadUrl("javascript:window.document.getElementById('tab').setAttribute('value', 'location');")
                }
            }
            webview.post(s);
        }
        @JavascriptInterface
        fun setState(s: String?){
            this@MainActivity.state=s
        }
    }

}