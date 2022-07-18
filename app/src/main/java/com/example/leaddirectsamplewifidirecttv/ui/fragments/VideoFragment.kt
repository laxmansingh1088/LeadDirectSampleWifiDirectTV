package com.example.leaddirectsamplewifidirecttv.ui.fragments

import android.app.ActivityManager
import android.content.Context.ACTIVITY_SERVICE
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.leaddirectsamplewifidirecttv.R
import com.example.leaddirectsamplewifidirecttv.databinding.FragmentVideoBinding
import com.example.leadp2pdirect.chatmessages.VideoCommands
import com.example.leadp2pdirect.chatmessages.enumss.VideoPlayBacks
import com.example.leadp2pdirect.servers.FileModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util

class VideoFragment : Fragment(R.layout.fragment_video) {

    private lateinit var binding: FragmentVideoBinding
    private lateinit var simpleExoPlayer: ExoPlayer
    private var playerView: StyledPlayerView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val filePathsList =
            requireArguments().getSerializable("filePathsList") as ArrayList<FileModel>
        Log.d("someeee", "$filePathsList")
        binding = getView().let { FragmentVideoBinding.bind(it!!) }
        playerView = binding.playerView
        playerView?.requestFocus();
        if (filePathsList != null) {
            activity?.runOnUiThread(Runnable { initializePlayer(filePathsList.get(0).absoluteFilePath) })
        }
    }

    private fun initializePlayer(path: String) {
        playerView?.visibility = View.VISIBLE
        val mediaDataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(requireContext())

        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(MediaItem.fromUri(path))

        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(mediaDataSourceFactory)

        if (!::simpleExoPlayer.isInitialized) {
            simpleExoPlayer = ExoPlayer.Builder(requireContext())
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
        }
        simpleExoPlayer.clearMediaItems()
        simpleExoPlayer.addMediaSource(mediaSource)

        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.playbackState
        // Prepare the player with the source.
        simpleExoPlayer.setMediaSource(mediaSource)
        simpleExoPlayer.prepare()
        playerView?.player = simpleExoPlayer
        playerView?.requestFocus()
    }


    fun handleChatMessage(videoCommands: VideoCommands?) {
        if (videoCommands != null) {
            when (videoCommands.playBacks) {
                VideoPlayBacks.PLAY -> {
                    playPlayer()
                }
                VideoPlayBacks.PAUSE -> {
                    pausePlayer()
                }
            }
        }
    }

    private fun releasePlayer() {
        if (simpleExoPlayer != null && ::simpleExoPlayer.isInitialized) {
            simpleExoPlayer.release()
        }
    }


    private fun playPlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.playWhenReady = true
        }
    }

    private fun pausePlayer() {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.playWhenReady = false
        }
    }


    fun showMemory() {

        // Declaring and Initializing the ActivityManager
        val actManager = activity?.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        // Declaring MemoryInfo object
        val memInfo = ActivityManager.MemoryInfo()
        // Fetching the data from the ActivityManager
        actManager.getMemoryInfo(memInfo)
        // Fetching the available and total memory and converting into Giga Bytes
        val availMemory = memInfo.availMem.toDouble() / (1024 * 1024 * 1024)
        val totalMemory = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        Toast.makeText(
            activity,
            "Availavle:- $availMemory\nTotal:- $totalMemory",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun seekTo(positionInMS: Long) {
        if (simpleExoPlayer != null) {
            simpleExoPlayer.seekTo(positionInMS)
        }
    }

     override fun onStart() {
        super.onStart()
    }

     override fun onResume() {
        super.onResume()
    }

     override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) releasePlayer()
    }

     override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) releasePlayer()

    }
}