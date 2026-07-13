package com.bestradio.app.ui.stations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationListScreen(
    country: Country,
    repository: StationsRepository,
    onStationSelected: (Station) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StationsViewModel = viewModel(factory = StationsViewModel.factory(repository, country))
    val stations by viewModel.stations.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(if (country == Country.FRANCE) "France" else "Maroc") })
        },
    ) { innerPadding ->
        if (stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Chargement des stations…")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(stations, key = { it.id }) { station ->
                    ListItem(
                        headlineContent = { Text(station.name) },
                        supportingContent = {
                            if (station.genre.isNotBlank()) Text(station.genre, maxLines = 1)
                        },
                        modifier = Modifier.clickable { onStationSelected(station) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
