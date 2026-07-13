package com.bestradio.app.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bestradio.app.data.local.FavoritesStore
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StationsViewModel(
    private val repository: StationsRepository,
    private val favoritesStore: FavoritesStore,
    private val country: Country,
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    val favoriteIds: StateFlow<Set<String>> = favoritesStore.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init {
        viewModelScope.launch {
            _stations.value = repository.getStations(country)
            repository.refresh(country)
            _stations.value = repository.getStations(country)
        }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { favoritesStore.toggleFavorite(stationId) }
    }

    companion object {
        fun factory(repository: StationsRepository, favoritesStore: FavoritesStore, country: Country) = viewModelFactory {
            initializer { StationsViewModel(repository, favoritesStore, country) }
        }
    }
}
