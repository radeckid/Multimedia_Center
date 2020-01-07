package com.damrad.multimediacenter.ui.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.damrad.multimediacenter.MainActivity
import com.damrad.multimediacenter.R
import kotlinx.android.synthetic.main.web_fragment.*
import kotlinx.android.synthetic.main.web_loading_progressbar.*
import java.util.jar.Manifest


class Web : Fragment() {

    companion object {
        fun newInstance() = Web()
    }

    private lateinit var viewModel: WebViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.web_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WebViewModel::class.java)
        // TODO: Use the ViewModel

        webview.settings.javaScriptEnabled = true
        webview.settings.setSupportZoom(true)
        webview.settings.builtInZoomControls = true

        webview.webChromeClient = object : WebChromeClient() {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                if (newProgress < 100 && loadingWebProgressBar?.visibility == View.GONE) {
                    loadingWebProgressBar.visibility = View.VISIBLE
                }

                pbText?.text = "${getString(R.string.please_wait)} $newProgress%"
            }
        }

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (loadingWebProgressBar?.visibility == View.VISIBLE) {
                    loadingWebProgressBar.visibility = View.GONE
                }
            }
        }
        webview.loadUrl("https://www.wiea.uz.zgora.pl/")
    }
}
