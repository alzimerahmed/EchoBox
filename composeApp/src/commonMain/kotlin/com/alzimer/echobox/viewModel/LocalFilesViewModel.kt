package com.alzimer.echobox.viewModel

import androidx.lifecycle.viewModelScope
import com.alzimer.echobox.domain.data.entities.SongEntity
import com.alzimer.echobox.domain.repository.LocalMediaRepository
import com.alzimer.echobox.viewModel.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalFilesViewModel(
    private val localMediaRepository: LocalMediaRepository,
) : BaseViewModel() {
    val localSongs: StateFlow<List<SongEntity>> =
        localMediaRepository.localSongs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> get() = _scanning.asStateFlow()

    fun rescan() {
        if (_scanning.value) return
        viewModelScope.launch {
            _scanning.value = true
            try {
                localMediaRepository.rescan()
            } finally {
                _scanning.value = false
            }
        }
    }
}
