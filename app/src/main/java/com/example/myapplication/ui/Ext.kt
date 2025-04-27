package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

inline fun ViewModel.launchOnIO(crossinline block: suspend () -> Unit): Job {
    return this.viewModelScope.launch(Dispatchers.IO) {
        block()
    }
}