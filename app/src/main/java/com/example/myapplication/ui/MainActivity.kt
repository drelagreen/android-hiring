package com.example.myapplication.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.network.SocketManager
import com.google.gson.Gson

private const val SERVER_ADDRESS = "challenge.ciliz.com"
private const val SERVER_PORT = 2222

class MainActivity : AppCompatActivity() {
    companion object {
        private const val SHARED_PREFS_NAME = "test_sharedpefs"
    }

    private val gson = Gson()
    private val socketManager = SocketManager(SERVER_ADDRESS, SERVER_PORT, gson)
    private val sharedWelcomeDialogViewModel: WelcomeDialogFragmentViewModel by viewModels {
        WelcomeDialogFragmentViewModelFactory(
            socketManager = socketManager,
            sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE),
        )
    }
    private var welcomeDialog: WelcomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )

        welcomeDialog = WelcomeFragment().apply {
            setIntentHandler(sharedWelcomeDialogViewModel::handleIntent)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, WelcomeFragment().apply {
                    setIntentHandler(sharedWelcomeDialogViewModel::handleIntent)
                })
                .addToBackStack(null)
                .commit()
        }

        lifecycleScope.launchWhenStarted {
            sharedWelcomeDialogViewModel.state.collect { state ->
                welcomeDialog?.setState(state)
            }
        }

        lifecycleScope.launchWhenStarted {
            sharedWelcomeDialogViewModel.effect.collect { effect ->
                when (effect) {
                    is WelcomeDialogFragmentViewModel.Effect.ShowError -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.error, effect.error),
                        Toast.LENGTH_SHORT
                    ).show()

                    is WelcomeDialogFragmentViewModel.Effect.ShowResult -> Toast.makeText(
                        this@MainActivity,
                        getString(
                            if (effect.allowed) {
                                R.string.welcome_dialog_success
                            } else {
                                R.string.welcome_dialog_failure
                            }
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
