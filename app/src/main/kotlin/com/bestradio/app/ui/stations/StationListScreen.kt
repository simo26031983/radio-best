package com.bestradio.app.ui.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import com.bestradio.app.util.foldForSearch

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
    var query by rememberSaveable { mutableStateOf("") }

    val filteredStations = remember(stations, query) {
        if (query.isBlank()) {
            stations
        } else {
            val foldedQuery = query.foldForSearch()
            stations.filter {
                it.name.foldForSearch().contains(foldedQuery) || it.genre.foldForSearch().contains(foldedQuery)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(if (country == Country.FRANCE) "France" else "Maroc") })
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Rechercher une station…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            if (stations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chargement des stations…")
                }
            } else if (filteredStations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune station ne correspond à « $query »")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredStations, key = { it.id }) { station ->
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
}
