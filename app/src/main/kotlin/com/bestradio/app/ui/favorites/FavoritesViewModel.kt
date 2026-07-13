package com.bestradio.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bestradio.app.data.local.FavoritesStore
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: StationsRepository,
    private val favoritesStore: FavoritesStore,
) : ViewModel() {

    private val allStations = MutableStateFlow<List<Station>>(emptyList())

    val favoriteStations: StateFlow<List<Station>> = combine(allStations, favoritesStore.favoriteIds) { all, ids ->
        all.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { allStations.value = repository.getAllStations() }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { favoritesStore.toggleFavorite(stationId) }
    }

    companion object {
        fun factory(repository: StationsRepository, favoritesStore: FavoritesStore) = viewModelFactory {
            initializer { FavoritesViewModel(repository, favoritesStore) }
        }
    }
}
