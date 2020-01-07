package com.damrad.multimediacenter.ui.camera

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Matrix
import android.os.*
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.Preview
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.PreviewConfig
import androidx.core.os.EnvironmentCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import com.damrad.multimediacenter.R
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.web_loading_progressbar.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class MyCamera : Fragment() {

    private lateinit var viewModel: CameraViewModel
    private var timer: Thread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CameraViewModel::class.java)

        startCamera()
    }

    private fun startCamera() {
        CameraX.unbindAll()

        val aspectRatio = Rational(textureView.width, textureView.height)
        val screen = Size(textureView.width, textureView.height)

        val pConfig: PreviewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetResolution(screen)
            //.setLensFacing(CameraX.LensFacing.FRONT)
            .build()

        val preview = Preview(pConfig)

        preview.onPreviewOutputUpdateListener = OnPreviewOutputUpdateListener { output ->
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.surfaceTexture = output.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            .setTargetRotation(activity?.windowManager?.defaultDisplay?.rotation!!).build()
        val imgCap = ImageCapture(imageCaptureConfig)

        takePhotoDelayTime(imgCap)

        //bind to lifecycle:
        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imgCap)

    }

    private fun updateTransform() {
        val mx = Matrix()
        val w = textureView.measuredWidth.toFloat()
        val h = textureView.measuredHeight.toFloat()

        val cX = w / 2f
        val cY = h / 2f

        val rotationDgr: Int
        val rotation = textureView.rotation.toInt()

        rotationDgr = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        mx.postRotate(rotationDgr.toFloat(), cX, cY)
        textureView.setTransform(mx)
    }

    private fun takePhotoDelayTime(image: ImageCapture) {
        val handler = Handler()
        val finishHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 123) {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/Camera/" + System.currentTimeMillis() + ".png")

                    image.takePicture(file, object : ImageCapture.OnImageSavedListener {
                        override fun onImageSaved(file: File) {
                            val msg2 = "Pic captured at " + file.absolutePath
                            Toast.makeText(context, msg2, Toast.LENGTH_LONG).show()
                        }

                        override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                            val msg2 = "Pic capture failed : $message"
                            Toast.makeText(context, msg2, Toast.LENGTH_LONG).show()

                            cause?.printStackTrace()
                        }

                    })
                }
            }
        }

        timer = Thread(Runnable {
            var i = 0
            while (i <= 5) {

                handler.post {
                    progressPhoto?.progress = i
                    timeText?.text = (5 - i).toString()
                }

                Thread.sleep(1000)

                i++
            }

            val messsage = finishHandler.obtainMessage(123)
            messsage.sendToTarget()
        })

        timer?.start()

    }

    override fun onPause() {
        super.onPause()

        timer?.interrupt()
    }
}
