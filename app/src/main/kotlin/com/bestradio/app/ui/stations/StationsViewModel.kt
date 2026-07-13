package com.bestradio.app.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bestradio.app.data.model.Country
import com.bestradio.app.data.model.Station
import com.bestradio.app.data.repository.StationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StationsViewModel(
    private val repository: StationsRepository,
    private val country: Country,
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    init {
        viewModelScope.launch {
            _stations.value = repository.getStations(country)
        }
    }

    companion object {
        fun factory(repository: StationsRepository, country: Country) = viewModelFactory {
            initializer { StationsViewModel(repository, country) }
        }
    }
}
