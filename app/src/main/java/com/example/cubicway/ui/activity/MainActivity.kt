package com.example.cubicway.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.cubicway.R
import org.opencv.android.NativeCameraView.TAG
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }
        setContentView(R.layout.activity_main)
        setButtonsActions()
    }

    private fun goesToSolveCube() {
        val intent = Intent(this@MainActivity, CubeSideRetrieverActivity::class.java)
        startActivity(intent)
    }

    private fun setButtonsActions() {
        val solveCubeButton = findViewById<AppCompatButton>(R.id.main_start_solve_cube)
        solveCubeButton.setOnClickListener {
            goesToSolveCube()
        }
    }
}