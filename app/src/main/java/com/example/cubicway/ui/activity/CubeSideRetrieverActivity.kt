package com.example.cubicway.ui.activity

import android.Manifest
import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cubicway.databinding.ActivityCubeSideRetrieverBinding
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class CubeSideRetrieverActivity : ComponentActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityCubeSideRetrieverBinding
    private lateinit var mIntermediateMat: Mat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkOpenCV(this)
        hideStatusBar()

        binding = ActivityCubeSideRetrieverBinding.inflate(layoutInflater)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(binding.root)


        // Request camera permissions
        if (allPermissionsGranted()) {
            checkOpenCV(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
        val cameraPreview = binding.cameraView
        cameraPreview.visibility = SurfaceView.VISIBLE
        cameraPreview.setCameraPermissionGranted()
        cameraPreview.setCvCameraViewListener(this)
        cameraPreview.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK)
        cameraPreview.enableView()
    }

    private fun checkOpenCV(context: Context) {
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }
    }

    companion object {
        private const val TAG = "OpenCV"
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                CAMERA,
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE,
            ).apply {
            }.toTypedArray()
        private const val tag = "CUBER"
        private const val REQUEST_CODE_PERMISSIONS = 111
        private val scannerAreaProportionLandscape = doubleArrayOf(0.15, 0.0, 0.85, 0.0)
        private val scannerAreaProportionPortrait = doubleArrayOf(0.15, 0.0, 0.85, 0.0)
    }

    override fun onPause() {
        super.onPause()
        if (::binding.isInitialized) {
            binding.cameraView.disableView()
        }
    }

    override fun onResume() {
        super.onResume()
        hideStatusBar()
        if (::binding.isInitialized) {
            binding.cameraView.enableView()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideStatusBar()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.cameraView.disableView()
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mIntermediateMat = Mat()
    }

    override fun onCameraViewStopped() {
        mIntermediateMat.release()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val image = inputFrame!!.rgba()
        return drawOutsideOfScanner(image)
    }

    private fun hideStatusBar() {
        window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }

    private fun getScannerArea(widthScreen: Int, heightScreen: Int): DoubleArray {
        var scannerArea = scannerAreaProportionLandscape

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            scannerArea = scannerAreaProportionPortrait
        }

        // add support for landscape and
        // change this wrong situation
        return doubleArrayOf(
            (widthScreen * scannerArea[0]),
            scannerArea[1],
            (widthScreen * scannerArea[2]),
            heightScreen.toDouble()
        )
    }
    private fun drawOutsideOfScanner(image: Mat): Mat {
        val newImage = Mat(image.rows(), image.cols(), image.type())
        val colorNotUsedArea = Scalar(0.0, 0.0, 0.0, 1.0)
        val blackImage = Mat(image.size(), image.type())
        image.copyTo(blackImage)

//        val scannerSquare = doubleArrayOf((image.width() * .15), 0.0, (image.width() * .85), image.height().toDouble())
        val scannerSquare = getScannerArea(image.width(), image.height())

        val p1SquareLeft = Point(0.0, 0.0)
        val p2SquareLeft = Point(scannerSquare[0], image.height().toDouble())
        Imgproc.rectangle(blackImage, p1SquareLeft, p2SquareLeft, colorNotUsedArea, -1)

        val p1SquareRight = Point(scannerSquare[2], 0.0)
        val p2SquareRight = Point(image.width().toDouble(), image.height().toDouble())
        Imgproc.rectangle(blackImage, p1SquareRight, p2SquareRight, colorNotUsedArea, -1)

        val p1SquareTop = Point(0.0, 0.0)
        val p2SquareTop = Point(image.width().toDouble(), scannerSquare[1])
        Imgproc.rectangle(blackImage, p1SquareTop, p2SquareTop, colorNotUsedArea, -1)

        val p1SquareBottom = Point(0.0, scannerSquare[3])
        val p2SquareBottom = Point(image.width().toDouble(), image.height().toDouble())
        Imgproc.rectangle(blackImage, p1SquareBottom, p2SquareBottom, colorNotUsedArea, -1)

        val alpha = 0.4
        Core.addWeighted(image, alpha, blackImage, 1 - alpha,0.0, newImage)

        return newImage
    }
}