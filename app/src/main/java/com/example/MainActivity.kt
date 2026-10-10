package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ladaroots.ui.LadaRootsApp
import com.example.ladaroots.ui.viewmodel.LadaRootsViewModel
import com.example.ui.theme.LadaRootsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LadaRootsTheme {
                val viewModel: LadaRootsViewModel = viewModel()
                LadaRootsApp(viewModel = viewModel)
            }
        }
    }
}
