package com.damrad.multimediacenter.ui.musicplayer

import android.annotation.SuppressLint
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.damrad.multimediacenter.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.music_player_bottom_sheet.*
import kotlinx.android.synthetic.main.music_player_fragment.*
import java.io.File
import kotlin.concurrent.thread


class MusicPlayer : Fragment() {

    companion object {
        fun newInstance() = MusicPlayer()
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var totalTime = 0

    private lateinit var viewModel: MusicPlayerViewModel
    private var songsList: ArrayList<HashMap<String, String>>? = null
    private var songId: Int = 0
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.music_player_fragment, container, false)
    }

    @SuppressLint("WrongConstant", "SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MusicPlayerViewModel::class.java)

        bottomSheetBehavior = BottomSheetBehavior.from(music_bottom_sheet)

        songsList = getPlayList(Environment.getExternalStorageDirectory().absolutePath)

        val tmpArray = mutableListOf<String>()

        for (item in songsList!!) {
            val name = item["file_name"].toString()
            tmpArray.add(name.substring(0, name.length - 4))
        }

        val listArrayAdapter: ArrayAdapter<String>? = this.context?.let { ArrayAdapter(it, android.R.layout.simple_list_item_1, tmpArray) }

        music_list.adapter = listArrayAdapter

        setSongAndSetText(songId)
        mediaPlayer.setVolume(1f, 1f)

        setOnClickButtons()
        setSeekBars()
    }

    private fun setOnClickButtons() {

        playBtn.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtn.setBackgroundResource(R.drawable.ic_pause)
            } else {
                mediaPlayer.pause()
                playBtn.setBackgroundResource(R.drawable.ic_play)
            }
        }

        nextSongBtn.setOnClickListener {
            nextSong()
        }

        previousSongBtn.setOnClickListener {
            previousSong()
        }

        songs_list.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }


        music_list.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val first = music_list?.getChildAt(0)
                    if (music_list?.firstVisiblePosition != 0 || first?.top != 0) {
                        v?.parent?.requestDisallowInterceptTouchEvent(true)
                    } else {
                        v?.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                }
            }

            v?.onTouchEvent(event)
            true
        }

        music_list.setOnItemClickListener { parent, view, position, id ->
            mediaPlayer.stop()
            setSongAndSetText(position)
            songId = position
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mediaPlayer.start()
            playBtn.setBackgroundResource(R.drawable.ic_pause)
        }
    }

    private fun setSeekBars() {
        val audioManager: AudioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager
        volumeBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        //Volume Bar Change Listener
        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    progress,
                    AudioManager.FLAG_SHOW_UI
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        //Position Bar Change Listener
        positionBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    positionBar.progress = progress
                }

                if (progress == totalTime) {
                    nextSong()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        //Thread to controll label and update postionbar
        val handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                val currentPosition = msg.what

                positionBar?.progress = currentPosition

                val elapsedTime = createTimeLabel(currentPosition)
                elapsedTimeLabel?.text = elapsedTime

                val remainingTime = createTimeLabel(totalTime - currentPosition)
                remainingTimeLabel?.text = "-$remainingTime"
            }
        }

        Thread(Runnable {
            val msg = Message()
            try {
                msg.what = mediaPlayer.currentPosition
                handler.sendMessage(msg)
                Thread.sleep(1000)
            } catch (ex: InterruptedException) {

            }
        }).start()
    }

    private fun createTimeLabel(time: Int): String {

        val min = time / 1000 / 60
        val sec = time / 1000 % 60

        var timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    private fun previousSong() {
        if (songId > 0) {
            songId--
        } else {
            songId = songsList?.size!!.minus(1)
        }

        mediaPlayer.stop()
        mediaPlayer.release()
        setSongAndSetText(songId)
        mediaPlayer.start()
        playBtn.setBackgroundResource(R.drawable.ic_pause)
    }

    private fun nextSong() {
        if (songId < songsList?.size!!.minus(1)) {
            songId++
        } else {
            songId = 0
        }

        mediaPlayer.stop()
        mediaPlayer.release()
        setSongAndSetText(songId)
        mediaPlayer.start()
        playBtn.setBackgroundResource(R.drawable.ic_pause)
    }

    private fun setSongAndSetText(songId: Int) {
        mediaPlayer = MediaPlayer.create(context, Uri.parse(songsList?.get(songId)?.get("file_path")))

        totalTime = mediaPlayer.duration
        positionBar.max = totalTime

        val playedSongName = songsList?.get(songId)?.get("file_name")
        songName.text = playedSongName?.substring(0, playedSongName.length - 4)
    }

    private fun getPlayList(rootPath: String): ArrayList<HashMap<String, String>> {
        val fileList: ArrayList<HashMap<String, String>> = ArrayList()
        val rootFolder = File(rootPath)

        val files = rootFolder.listFiles()

        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    fileList.addAll(getPlayList(file.absolutePath))
                } else if (file.name.endsWith(".mp3") && (!file.name.startsWith("."))) {
                    val song: HashMap<String, String> = HashMap()
                    song["file_path"] = file.absolutePath
                    song["file_name"] = file.name
                    fileList.add(song)
                }
            }
        }
        return fileList
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    override fun onResume() {
        super.onResume()
        playBtn.setBackgroundResource(R.drawable.ic_play)
        setSongAndSetText(0)
    }
}

