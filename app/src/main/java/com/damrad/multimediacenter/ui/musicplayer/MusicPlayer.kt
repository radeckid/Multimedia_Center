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
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.damrad.multimediacenter.R
import kotlinx.android.synthetic.main.music_player_fragment.*
import java.io.File


class MusicPlayer : Fragment() {

    companion object {
        fun newInstance() = MusicPlayer()
    }

    private lateinit var mediaPlayer: MediaPlayer
    private var totalTime = 0

    private lateinit var viewModel: MusicPlayerViewModel
    private lateinit var songsList: ArrayList<HashMap<String, String>>
    private var songId: Int = 0

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
        // TODO: Use the ViewModel

        songsList = getPlayList(Environment.getExternalStorageDirectory().absolutePath)

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
            if (songId > 0) {
                songId--
            } else {
                songId = songsList.size - 1
            }

            mediaPlayer.stop()
            setSongAndSetText(songId)
            mediaPlayer.start()
            playBtn.setBackgroundResource(R.drawable.ic_pause)
        }
    }

    private fun setSeekBars() {
        val audioManager: AudioManager = context?.getSystemService(AUDIO_SERVICE) as AudioManager

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
            while (mediaPlayer != null) {
                val msg = Message()
                msg.what = mediaPlayer.currentPosition
                handler.sendMessage(msg)
                Thread.sleep(500)
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

    private fun nextSong() {
        if (songId < songsList.size - 1) {
            songId++
        } else {
            songId = 0
        }

        mediaPlayer.stop()
        setSongAndSetText(songId)
        mediaPlayer.start()
        playBtn.setBackgroundResource(R.drawable.ic_pause)
    }

    private fun setSongAndSetText(songId: Int) {
        mediaPlayer = MediaPlayer.create(context, Uri.parse(songsList[songId]["file_path"]))

        totalTime = mediaPlayer.duration
        positionBar.max = totalTime

        val playedSongName = songsList[songId]["file_name"]
        songName.text = playedSongName?.substring(0, playedSongName.length - 3)
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
}

