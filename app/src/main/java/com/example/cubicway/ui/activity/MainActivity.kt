package com.example.cubicway.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.cubicway.R
import com.example.cubicway.ui.theme.CubicWayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonsActions()
    }

    private fun goesToSolveCube() {
        val intent = Intent(this@MainActivity, SolveCube::class.java)
        startActivity(intent)
    }

    private fun setButtonsActions() {
        val solveCubeButton = findViewById<AppCompatButton>(R.id.main_start_solve_cube)
        solveCubeButton.setOnClickListener {
            goesToSolveCube()
        }
    }
}