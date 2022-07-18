package layout

import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.databinding.FragmentWebviewBinding

class WebViewFragment : Fragment(R.layout.fragment_webview) {
    private val isMultipleH5P = false
    private lateinit var binding: FragmentWebviewBinding
    private var webView: WebView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val indexFilePath = requireArguments().getString("ir_res_path_index")
        binding = getView().let { FragmentWebviewBinding.bind(it!!) }
        webView = binding.webView

        webView?.settings?.javaScriptEnabled = true
        webView?.getSettings()?.mediaPlaybackRequiresUserGesture = false;
        webView?.clearHistory();
        webView?.clearFormData();
        webView?.clearCache(true);
        webView?.settings?.setSupportZoom(true)
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.setAllowFileAccessFromFileURLs(true)
        webView?.settings?.allowFileAccess = true
        webView?.settings?.setAllowUniversalAccessFromFileURLs(true)
        webView?.settings?.setSupportMultipleWindows(true)
        webView?.settings?.setAppCacheEnabled(true)
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.settings?.textZoom = 100
        webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView?.webChromeClient = WebChromeClient()
        webView?.requestFocusFromTouch()

        activity?.runOnUiThread(Runnable {
            if (indexFilePath != null) {
                webView?.loadUrl("file:///$indexFilePath")
            } else {
                webView?.loadUrl("https://www.youtube.com")
            }

            webView?.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (!isMultipleH5P) {
                        webView?.loadUrl(url)
                    }
                    return false
                }
            }
        })
    }


/*
    webView.settings.setJavaScriptEnabled(true)
    webView.settings.setDomStorageEnabled(true)
    webView.settings.setAllowFileAccessFromFileURLs(true)
    webView.settings.setAllowFileAccess(true)
    webView.settings.setAllowUniversalAccessFromFileURLs(true)
    webView.settings.setSupportMultipleWindows(true)
    webView.settings.setAppCacheEnabled(true)
    webView.settings.setJavaScriptCanOpenWindowsAutomatically(true)
    webView.settings.setTextZoom(100)
//        webView.loadUrl("file:///android_asset/IR-1-Start/IR-1-Start.html");
    //        webView.loadUrl("file:///android_asset/IR-1-Start/IR-1-Start.html");
    webView.loadUrl(url)
    webView.setWebViewClient(
    object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (!isMultipleH5P) {
                webView.loadUrl(url)
            }
            return false
        }
    })
//        webView.setWebChromeClient(new WebChromeClient());
    //        webView.setWebChromeClient(new WebChromeClient());
    webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
    webView.requestFocusFromTouch()*/
}