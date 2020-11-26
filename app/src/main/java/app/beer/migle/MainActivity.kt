package app.beer.migle

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity(), OnProgressListener {

    private var url = "https://migle.by/"
    private lateinit var webView: WebView

    private lateinit var noInternetLabel: ConstraintLayout
    private lateinit var loader: ConstraintLayout

    private var haveInternet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideUserBars()
        init()
    }

    private fun init() {
        webView = findViewById(R.id.web_view)
        noInternetLabel = findViewById(R.id.not_internet)
        loader = findViewById(R.id.loader)
        loader.visibility = View.GONE

        // add clients for webView
        webView.webViewClient = MyWebViewClient()
        webView.webChromeClient = MyChromeWebClient(this)

        webView.setNetworkAvailable(true)

        // enabled javaScript and other functions
        webView.settings.javaScriptEnabled = true
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.setAppCacheEnabled(true)

        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }
        webView.loadUrl(url)
    }

    private fun hideUserBars() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    override fun onResume() {
        super.onResume()
        haveInternet = hasConnection()
        if (haveInternet) {
            noInternetLabel.visibility = View.GONE
            webView.visibility = View.VISIBLE
        } else {
            webView.visibility = View.GONE
            noInternetLabel.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else
            super.onBackPressed()
    }

    private fun hasConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        return wifiInfo != null && wifiInfo.isConnected
    }

    private inner class MyWebViewClient : WebViewClient() {

        @TargetApi(Build.VERSION_CODES.M)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }

        @SuppressLint("deprecated")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (url != null) {
                view?.loadUrl(url)
            }
            return true
        }

    }

    private inner class MyChromeWebClient(val listener: OnProgressListener) : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            listener.onUpdateProgress(newProgress)
        }
    }

    override fun onUpdateProgress(progress: Int) {
        loader.visibility = View.VISIBLE
        webView.visibility = View.GONE
        if (progress == 100) {
            webView.visibility = View.VISIBLE
            loader.visibility = View.GONE
        }
    }

}