package com.example.rentease.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rentease.R
import com.example.rentease.ui.helpers.WindowInsetsHelper
import com.google.android.material.appbar.MaterialToolbar

/**
 * WebViewFragment displays the RentEase web application within the mobile app.
 * This demonstrates the integration between mobile and web technologies.
 */
class WebViewFragment : Fragment() {

    private lateinit var webView: WebView
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_webview, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply window insets for proper edge-to-edge display
        val appBarLayout = view.findViewById<View>(R.id.appBarLayout)
        WindowInsetsHelper.applyWindowInsets(view, appBarLayout)

        setupToolbar()
        setupWebView()
        loadWebApplication()
    }

    private fun setupToolbar() {
        val toolbar = rootView.findViewById<MaterialToolbar>(R.id.toolbar)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back button click
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView = rootView.findViewById(R.id.webView)

        // Configure WebView settings
        webView.settings.apply {
            // Enable JavaScript for dynamic content (required for web application)
            javaScriptEnabled = true

            // Enable DOM storage for web application functionality
            domStorageEnabled = true

            // Enable built-in zoom controls
            builtInZoomControls = true
            displayZoomControls = false

            // Improve rendering
            useWideViewPort = true
            loadWithOverviewMode = true

            // Security settings
            allowFileAccess = false
            allowContentAccess = false
        }

        // Set up WebViewClient to handle page loading
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                showError("Failed to load web application: ${error?.description}")
            }
        }

        // Set up WebChromeClient to handle progress updates and title
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                val progressBar = rootView.findViewById<android.widget.ProgressBar>(R.id.progressBar)
                progressBar.progress = newProgress

                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                // Set custom title to show RentEase branding instead of "Web Portal"
                (activity as? AppCompatActivity)?.supportActionBar?.title = "🏠 RentEase"
            }
        }
    }

    private fun loadWebApplication() {
        // Load the web application
        // Using localhost:8000 with ADB reverse port forwarding (same as API client)
        val webUrl = "http://localhost:8000/web/"
        webView.loadUrl(webUrl)
    }

    private fun showLoading() {
        rootView.findViewById<View>(R.id.loadingIndicator).visibility = View.VISIBLE
        rootView.findViewById<View>(R.id.webView).visibility = View.GONE
        rootView.findViewById<View>(R.id.errorLayout).visibility = View.GONE
    }

    private fun hideLoading() {
        rootView.findViewById<View>(R.id.loadingIndicator).visibility = View.GONE
        rootView.findViewById<View>(R.id.webView).visibility = View.VISIBLE
        rootView.findViewById<View>(R.id.errorLayout).visibility = View.GONE
    }

    private fun showError(message: String) {
        rootView.findViewById<View>(R.id.loadingIndicator).visibility = View.GONE
        rootView.findViewById<View>(R.id.webView).visibility = View.GONE
        rootView.findViewById<View>(R.id.errorLayout).visibility = View.VISIBLE
        rootView.findViewById<android.widget.TextView>(R.id.errorMessage).text = message

        // Set up retry button
        rootView.findViewById<android.widget.Button>(R.id.retryButton).setOnClickListener {
            loadWebApplication()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up WebView
        if (::webView.isInitialized) {
            webView.destroy()
        }
    }

    companion object {
        fun newInstance() = WebViewFragment()
    }
}
