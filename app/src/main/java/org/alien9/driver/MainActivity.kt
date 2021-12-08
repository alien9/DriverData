package org.alien9.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
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
import android.view.*
import android.webkit.*
import android.webkit.WebView.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.webkit.WebViewAssetLoader
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.google.android.gms.location.LocationServices
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.views.MapView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import android.widget.AdapterView.OnItemSelectedListener as OnItemSelectedListener1

private const val FCR = 1
class MainActivity : AppCompatActivity() {
    private var locator: String?="Fused"
    private var POSITION: Int=333
    private lateinit var dataset: JSONArray
    private var currentRecordIndex: Int = 0
    private lateinit var token: String
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
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(true)
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


        var mUploadMessage: ValueCallback<Uri>
        Companion.FILECHOOSER_RESULTCODE = 1

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
        webSettings.loadWithOverviewMode=true
        //webSettings.cacheMode=WebSettings.LOAD_CACHE_ELSE_NETWORK
        webSettings.setGeolocationEnabled(true)
        mWebview.addJavascriptInterface(JsWebInterface(this, mWebview), "androidApp")
        val context=this
        val assetLoader=WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/driver-angular/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(this))
            .build()
        var sharedPref: SharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        val b = sharedPref.getString("backend", getString(R.string.backend) ).toString()
        mWebview.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(webView:WebView, filePathCallback:ValueCallback<Array<Uri>>, fileChooserParams:FileChooserParams):Boolean {
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    takePictureIntent.resolveActivity(packageManager)?.also {
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
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)

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

        }
        mWebview.webViewClient = LocalContentWebClient(assetLoader, b)
        mWebview.settings.allowUniversalAccessFromFileURLs=true
        val activity=this@MainActivity
        sharedPref = activity.getPreferences(Context.MODE_PRIVATE)
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
            var byteArray = bas.toByteArray()
            var ext=Base64.encodeToString(byteArray, Base64.DEFAULT)
            var j= JSONObject()
            j.put("src", ext)
            var u="javascript:(document.getElementById('file-name')).setAttribute('value', '${currentPhotoPath}');"
            findViewById<WebView>(R.id.webview).loadUrl(u)
            u="javascript:var t=document.getElementById('image-loader');t.setAttribute('value', '${ext}');var event = new Event('change');t.dispatchEvent(event);"
            findViewById<WebView>(R.id.webview).loadUrl(u)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val w= findViewById<WebView>(R.id.webview)
        w.evaluateJavascript("(function(){return $('#state').val()})();") { s ->
            val state=s.replace("\"", "")
            menu?.findItem(R.id.take_photo)?.isVisible = false //state == "input"
            menu?.findItem(R.id.action_refresh)?.isVisible = false
            menu?.findItem(R.id.reload)?.isVisible = false
            menu?.findItem(R.id.action_map)?.isVisible = state == "input" || state == "locate"
            menu?.findItem(R.id.action_logout)?.isVisible = true
            menu?.findItem(R.id.upload)?.isVisible = state == "list"
            menu?.findItem(R.id.action_logout)?.isVisible = state != "login"

        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun place(it: Location?){
        findViewById<WebView>(R.id.webview).evaluateJavascript(
            """   
(function(){
$('input[name=position]').val('${it?.latitude},${it?.longitude}');
$('input[name=position]').click();
})();
"""
        ) {
        }
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
                val w= findViewById<WebView>(R.id.webview)
                w.clearCache(true)
                w.loadUrl("javascript:localStorage.setItem('backend', '%s')".format(backend))
                val a=this@MainActivity
                val sharedPref = a.getPreferences(Context.MODE_PRIVATE)
                backend = sharedPref.getString("backend", getString(R.string.backend))
                frontend = sharedPref.getString("frontend",  getString(R.string.frontend))
                w.loadUrl("${frontend}/index.html")
                true
            }
            R.id.take_photo->{
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                takePictureIntent.resolveActivity(packageManager)?.also {
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
                val inflater = layoutInflater
                builder.setTitle("DRIVER Settings")
                builder.setMessage("host")
                val dialogLayout = inflater.inflate(R.layout.alert_edit, null)
                val editText = dialogLayout.findViewById<EditText>(R.id.editText)
                val frontendText = dialogLayout.findViewById<EditText>(R.id.frontendText)
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
                backend = sharedPref.getString("backend", getString(R.string.backend))
                frontend = sharedPref.getString("frontend", getString(R.string.frontend))
                locator=sharedPref.getString("locator", "Fused")
                editText.setText(backend)
                frontendText.setText(frontend)
                builder.setView(dialogLayout)

                var spinner = dialogLayout.findViewById<Spinner>(R.id.locationBackend)
                ArrayAdapter.createFromResource(
                    this,
                    R.array.location_datasets,
                    android.R.layout.simple_spinner_item
                ).also {
                    adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    var vs=resources.getStringArray(R.array.location_datasets)
                    spinner.setSelection(vs.indexOf(locator))
                }
                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                    val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
                    backend=editText.text.toString()
                    with (sharedPref.edit()) {
                        putString("backend", backend)
                        putString("frontend", frontendText.text.toString())
                        putString("locator", spinner.getItemAtPosition(spinner.selectedItemPosition).toString())
                        apply()
                    }
                    (findViewById<WebView>(R.id.webview)).clearCache(true)
                    (findViewById<WebView>(R.id.webview)).evaluateJavascript("""
                        (function(){
                        localStorage.setItem('backend', '${backend}');
                        })();
                    """) {

                    }
                }

                builder.setNegativeButton(android.R.string.cancel) { dialog, which ->

                }
                builder.show()
                true
            }
            R.id.action_logout -> {
                (findViewById<WebView>(R.id.webview)).loadUrl("javascript:localStorage.clear()")
                (findViewById<WebView>(R.id.webview)).loadUrl("javascript:$('#list-logout-button').click();")
                (findViewById<WebView>(R.id.webview)).evaluateJavascript("""
                        (function(){
                        localStorage.setItem('backend', '${backend}');
                        })();
                    """) {

                }
                true
            }
            R.id.cleanup->{
                (findViewById<WebView>(R.id.webview)).evaluateJavascript("(function(){$('#cleanup-button').click()})();") { }
                true
            }
            R.id.upload->{
                (findViewById<WebView>(R.id.webview)).evaluateJavascript("(function(){return localStorage.getItem('token');})();") { s ->
                    token=s.replace("\"", "")
                }
                (findViewById<WebView>(R.id.webview)).evaluateJavascript("(function(){return JSON.parse($('#dataset').val())})();") { s ->
                    dataset = JSONArray(s)
                    currentRecordIndex=0
                    if(currentRecordIndex<dataset.length()){
                        this.upload()
                    }
                }
                true
            }
            R.id.action_map->{
                val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE)
                locator=sharedPref.getString("locator", "Fused")
                when(locator){
                    "Fused" -> {
                        val fusedLocationClient = (applicationContext as Driver).fusedLocationClient
                        fusedLocationClient.lastLocation.addOnSuccessListener {
                            if (it != null) {
                                place(it)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Location returned was null",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                        true
                    }
                    "GPS" -> {
                        val loca = (applicationContext as Driver).loca as LocationManager
                        loca.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).also {
                            if (it != null) {
                                place(it)
                            }
                        }
                    }
                    "Network" -> {
                        val loca = (applicationContext as Driver).loca as LocationManager
                        loca.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).also {
                            if (it != null) {
                                place(it)
                            }
                        }
                    }

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun upload() {
        if(currentRecordIndex>=dataset.length()) {
            (findViewById<View>(R.id.webview)).visibility = VISIBLE
            (findViewById<View>(R.id.progressBar)).visibility = GONE
            return
        }
        val jo: JSONObject=dataset.getJSONObject(currentRecordIndex)
        (findViewById<View>(R.id.webview)).visibility = GONE
        (findViewById<View>(R.id.progressBar)).visibility = VISIBLE
        val queue = Bowser.getInstance(this.applicationContext).requestQueue
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        backend = sharedPref.getString("backend", getString(R.string.backend))
        val url = "${backend}/api/records/"
        val listener= Listener<JSONObject> { it ->
            dataset.getJSONObject(currentRecordIndex).put("uploaded",true)
            currentRecordIndex++
            if(currentRecordIndex<dataset.length()) {
                upload()
            }else {
                (findViewById<View>(R.id.webview)).visibility = VISIBLE
                (findViewById<View>(R.id.progressBar)).visibility = GONE
            }
            (findViewById<WebView>(R.id.webview)).evaluateJavascript("""
                    (function(){
                    console.log('$dataset');
                    localStorage.setItem('dataset','$dataset');
                    $("#dataset").click();
                    })();
                """){ }


        }
        val errorListener=Response.ErrorListener {
            val mess=JSONObject(String(it.networkResponse.data))
            if(mess.has("data")){
                Toast.makeText(this, mess.getString("data"), Toast.LENGTH_LONG).show()
            }else {
                if(mess.has("schema")){
                    Toast.makeText(this, mess.optJSONArray("schema").join(""), Toast.LENGTH_LONG).show()
                }else {
                    if(mess.has("detail")){
                        Toast.makeText(this, mess.optString("detail"), Toast.LENGTH_LONG).show()
                    }else {
                        Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
            (findViewById<View>(R.id.webview)).visibility = VISIBLE
            (findViewById<View>(R.id.progressBar)).visibility = GONE
        }
        if(!jo.optBoolean("uploaded", false)) {
            val jsonObjectRequest = DriverRequest<JSONArray>(
                Request.Method.POST, url, jo.getJSONObject("record").toString(),
                errorListener, listener
            )
            jsonObjectRequest.setToken(token)
            queue.add(jsonObjectRequest)
        }else{
            currentRecordIndex++
            upload()
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
                    webview.loadUrl(u)
//                    webview.loadUrl("javascript:window.document.getElementById('tab').setAttribute('value', 'location');")
                }
            }
            webview.post(s)
        }
        @JavascriptInterface
        fun setState(s: String?){
            this@MainActivity.state=s
        }
    }
}