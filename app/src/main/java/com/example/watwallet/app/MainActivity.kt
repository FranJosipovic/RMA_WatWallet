package com.example.watwallet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.ui.theme.WatWalletTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository: AuthRepository by inject()

        lifecycleScope.launch {

            val startingRoute = if(authRepository.isAuthenticated()){
                NavigationItem.Main.route
            }else{
                NavigationItem.Auth.route
            }


            setContent {
                WatWalletTheme {
                    MainNavigation(
                        startingRoute = startingRoute
                    )
                }
            }
        }
    }
}
