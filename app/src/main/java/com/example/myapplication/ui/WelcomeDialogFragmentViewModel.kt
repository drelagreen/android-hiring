package com.example.myapplication.ui

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.network.SocketManager
import com.example.myapplication.network.TestRequest
import com.example.myapplication.ui.WelcomeDialogFragmentViewModel.State.Gender
import com.example.myapplication.ui.WelcomeDialogFragmentViewModel.State.Gender.entries
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class WelcomeDialogFragmentViewModelFactory(
    val socketManager: SocketManager,
    val sharedPreferences: SharedPreferences,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WelcomeDialogFragmentViewModel::class.java)) {
            return WelcomeDialogFragmentViewModel(socketManager, sharedPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class WelcomeDialogFragmentViewModel(
    val socketManager: SocketManager,
    val sharedPreferences: SharedPreferences,
) : ViewModel() {
    companion object {
        const val DEFAULT_AGE = 0
        const val AGE_TAG = "age"
        const val GENDER_TAG = "gender"
    }

    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> get() = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    var socketJob: Job? = null

    init {
        launchOnIO {
            val age = sharedPreferences.getInt(AGE_TAG, DEFAULT_AGE)
            val genderId = sharedPreferences.getInt(GENDER_TAG, 0)

            _state.emit(
                State(
                    age = age,
                    gender = Gender.fromId(genderId),
                    isButtonEnabled = false
                )
            )
        }
    }

    fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.OnContinueClick -> {
                val currentState = _state.value
                if (socketJob?.isActive == true) return

                socketJob = launchOnIO {
                    _state.emit(
                        currentState.copy(
                            isButtonEnabled = false,
                        )
                    )

                    try {
                        socketManager.connect()
                        socketManager.send(
                            currentState.toTestRequest()
                        )
                        val response = socketManager.receive()
                        socketManager.close()

                        _effect.send(Effect.ShowResult(response.allowed))
                    } catch (e: Exception) {
                        Log.e(this::class.simpleName, "Socket send: ", e)
                        _effect.send(Effect.ShowError(e.message.toString()))
                    }

                    _state.emit(
                        currentState.copy(
                            isButtonEnabled = true,
                        )
                    )

                    socketJob = null
                }
            }

            is Intent.OnAgeChange -> launchOnIO {
                Log.i("TAG", "handleIntent: OnAgeChange")
                _state.emit(
                    _state.value.copy(
                        age = intent.age,
                        isButtonEnabled = _state.value.gender != Gender.UNKNOWN,
                    )
                )
                sharedPreferences.edit(commit = true) {
                    putInt(AGE_TAG, intent.age)
                }
            }

            is Intent.OnGenderChange -> launchOnIO {
                Log.i("TAG", "handleIntent: OnGenderChange")

                _state.emit(
                    _state.value.copy(
                        gender = intent.gender,
                        isButtonEnabled = _state.value.age != 0,
                    )
                )
                sharedPreferences.edit(commit = true) {
                    putInt(GENDER_TAG, intent.gender.id)
                }
            }
        }
    }

    sealed interface Intent {
        data class OnAgeChange(val age: Int) : Intent
        data class OnGenderChange(val gender: Gender) : Intent
        data object OnContinueClick : Intent
    }

    sealed interface Effect {
        data class ShowError(val error: String) : Effect
        data class ShowResult(val allowed: Boolean) : Effect
    }

    data class State(
        val age: Int = DEFAULT_AGE,
        val gender: Gender = Gender.UNKNOWN,
        val isButtonEnabled: Boolean = false,
    ) {
        enum class Gender(val id: Int) {
            MALE(1), FEMALE(2), UNKNOWN(0);

            companion object {
                fun fromId(id: Int) = entries.firstOrNull { it.id == id } ?: UNKNOWN
            }
        }
    }

    private fun State.toTestRequest(): TestRequest {
        return TestRequest(
            gender = when (this.gender) {
                Gender.MALE -> "m"
                Gender.FEMALE -> "f"
                Gender.UNKNOWN -> ""
            },
            age = this.age
        )
    }
}