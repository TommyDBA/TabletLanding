package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    private lateinit var myWebView: WebView

    // landing page on my GitHub
    private val githubUrl = "https://raw.githubusercontent.com/TommyDBA/TabletLanding/main/homePage.html"

    // The default URL to use if the GitHub file can't be found.
    private val fallbackUrl = "https://forms.monday.com/forms/ddf346b988a73cfcb07616d66579505b"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        myWebView = WebView(this)
        val settings: WebSettings = myWebView.settings
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        setContentView(myWebView)

        // Start the process of loading the correct URL
        loadLandingPage()
    }

    private fun loadLandingPage() {
        // Use a coroutine to perform the network request on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Attempt to fetch the HTML content from the GitHub URL
                val htmlContent = getHtmlFromUrl(githubUrl)
                if (htmlContent != null) {
                    // If content is found, load it directly into the WebView
                    withContext(Dispatchers.Main) {
                        myWebView.loadDataWithBaseURL(githubUrl, htmlContent, "text/html", "UTF-8", null)
                    }
                } else {
                    // If content is not found (e.g., 404), load the fallback URL
                    withContext(Dispatchers.Main) {
                        myWebView.loadUrl(fallbackUrl)
                    }
                }
            } catch (e: Exception) {
                // Handle any other network errors by falling back
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    myWebView.loadUrl(fallbackUrl)
                }
            }
        }
    }

    private fun getHtmlFromUrl(urlString: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000 // 5 seconds
            connection.readTimeout = 5000 // 5 seconds

            val responseCode = connection.responseCode
            // Check if the request was successful (HTTP 200 OK)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the HTML content from the input stream
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val content = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line).append("\n")
                }
                return content.toString()
            }
        } catch (e: Exception) {
            // Log any errors that occur during the network request
            e.printStackTrace()
        } finally {
            // Make sure the connection is closed
            connection?.disconnect()
        }
        return null
    }



}

