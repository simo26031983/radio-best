package com.bestradio.app.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bestradio.app.data.local.FavoritesStore
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import com.bestradio.app.ui.stations.StationRow

@Composable
fun FavoritesScreen(
    repository: StationsRepository,
    favoritesStore: FavoritesStore,
    onStationSelected: (Station) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModel.factory(repository, favoritesStore))
    val favoriteStations by viewModel.favoriteStations.collectAsState()

    if (favoriteStations.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Aucun favori pour l'instant — appuyez sur ☆ à côté d'une station pour l'ajouter.")
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(favoriteStations, key = { it.id }) { station ->
                StationRow(
                    station = station,
                    isFavorite = true,
                    onClick = { onStationSelected(station) },
                    onToggleFavorite = { viewModel.toggleFavorite(station.id) },
                )
                HorizontalDivider()
            }
        }
    }
}
