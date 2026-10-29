package com.touti.score

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.touti.score.ui.GameViewModel
import com.touti.score.ui.ToutiApp
import com.touti.score.ui.ToutiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // The phone sits on the table for the whole game: don't let it go to sleep.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            ToutiTheme {
                ToutiApp(viewModel<GameViewModel>())
            }
        }
    }
}
