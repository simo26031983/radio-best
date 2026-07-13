package com.bestradio.app

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bestradio.app.data.model.Country
import com.bestradio.app.di.AppContainer
import com.bestradio.app.ui.favorites.FavoritesScreen
import com.bestradio.app.ui.player.MiniPlayerBar
import com.bestradio.app.ui.player.PlayerViewModel
import com.bestradio.app.ui.stations.StationListScreen
import com.bestradio.app.ui.theme.BestRadioTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as BestRadioApplication).container
        setContent {
            BestRadioTheme {
                BestRadioApp(container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BestRadioApp(container: AppContainer) {
    val application = LocalContext.current.applicationContext as Application

    val playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModel.factory(application, container.stationsRepository, container.playbackStateStore),
    )
    val playerState by playerViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            playerState.currentStation?.let { station ->
                MiniPlayerBar(
                    stationName = station.name,
                    stationFaviconUrl = station.faviconUrl,
                    isPlaying = playerState.isPlaying,
                    onTogglePlayPause = playerViewModel::togglePlayPause,
                )
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("France") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Maroc") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Favoris") })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when (selectedTab) {
                0 -> StationListScreen(
                    country = Country.FRANCE,
                    repository = container.stationsRepository,
                    favoritesStore = container.favoritesStore,
                    onStationSelected = playerViewModel::play,
                    modifier = Modifier.fillMaxSize(),
                )
                1 -> StationListScreen(
                    country = Country.MOROCCO,
                    repository = container.stationsRepository,
                    favoritesStore = container.favoritesStore,
                    onStationSelected = playerViewModel::play,
                    modifier = Modifier.fillMaxSize(),
                )
                else -> FavoritesScreen(
                    repository = container.stationsRepository,
                    favoritesStore = container.favoritesStore,
                    onStationSelected = playerViewModel::play,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
