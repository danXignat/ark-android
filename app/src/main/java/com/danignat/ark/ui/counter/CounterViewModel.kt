package com.danignat.ark.ui.counter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danignat.ark.data.CounterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CounterViewModel @Inject constructor(
    private val counterRepository: CounterRepository
) : ViewModel() {

    val counter: StateFlow<Int> = counterRepository.counter.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun increment() {
        counterRepository.increment()
    }

    fun decrement() {
        counterRepository.decrement()
    }

    fun reset() {
        counterRepository.reset()
    }
}

