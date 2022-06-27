package com.example.leaddirectsamplewifidirecttv.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.leaddirectsamplewifidirecttv.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import java.io.InputStream

class Temp : FragmentActivity() {


    private lateinit var simpleExoPlayer: ExoPlayer
    private var btn_image: Button? = null
    private var btn_video: Button? = null
    private var btn_webview: Button? = null
    private var btn_pdf: Button? = null
    private var pdfView: PDFView? = null
    private var imageView: ImageView? = null
    private var playerView: PlayerView? = null
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        btn_image = findViewById(R.id.btn_image)
        btn_video = findViewById(R.id.btn_video)
        btn_webview = findViewById(R.id.btn_webview)
        btn_pdf = findViewById(R.id.btn_pdf)
        pdfView = findViewById(R.id.pdfView)
        imageView = findViewById(R.id.imageView)
        webView = findViewById(R.id.webView)

/*
        btn_image?.setOnClickListener(View.OnClickListener {
            val path = "android.resource://" + packageName + "/" + R.raw.sample_video
            Log.d("pathhh",path);
            showImage(path)
        })
        btn_pdf?.setOnClickListener(View.OnClickListener {
            val path = "android.resource://" + packageName + "/" + R.raw.get_started_with_smallpdf
            Log.d("pathhh",path)
            val raw: InputStream = this.getAssets().open("sample3.pdf")
            showPdf(path,raw)
        })
        btn_video?.setOnClickListener(View.OnClickListener {
            val path = "android.resource://" + packageName + "/" + R.raw.sample_video
            Log.d("pathhh",path);
            initializePlayer(path)
        })

        btn_webview?.setOnClickListener(View.OnClickListener {
            val path = "android.resource://" + packageName + "/" + R.raw.get_started_with_smallpdf
            Log.d("pathhh",path);
            loadWebview(path)
        })*/
    }





}