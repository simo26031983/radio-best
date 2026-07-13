package com.bestradio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.repository.StationsRepository
import com.bestradio.app.ui.country.CountryPickerScreen
import com.bestradio.app.ui.player.MiniPlayerBar
import com.bestradio.app.ui.player.PlayerViewModel
import com.bestradio.app.ui.stations.StationListScreen
import com.bestradio.app.ui.theme.BestRadioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as BestRadioApplication).container.stationsRepository
        setContent {
            BestRadioTheme {
                BestRadioApp(repository)
            }
        }
    }
}

@Composable
private fun BestRadioApp(repository: StationsRepository) {
    var selectedCountry by remember { mutableStateOf<Country?>(null) }
    val playerViewModel: PlayerViewModel = viewModel()
    val playerState by playerViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            playerState.currentStation?.let { station ->
                MiniPlayerBar(
                    stationName = station.name,
                    isPlaying = playerState.isPlaying,
                    onTogglePlayPause = playerViewModel::togglePlayPause,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val country = selectedCountry
            if (country == null) {
                CountryPickerScreen(
                    onCountrySelected = { selectedCountry = it },
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                StationListScreen(
                    country = country,
                    repository = repository,
                    onStationSelected = playerViewModel::play,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
