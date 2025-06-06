package com.example.watwallet.feature.auth.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.feature.auth.viewmodel.LoginFormEvent
import com.example.watwallet.feature.auth.viewmodel.LoginViewModel
import com.example.watwallet.ui.components.LabeledInputField
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(navController: NavController) {

    val loginViewModel: LoginViewModel = koinViewModel()

    val loginState by loginViewModel.loginState.collectAsState()
    val loading by loginViewModel.loading.collectAsState()

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 100.dp, horizontal = 20.dp)
        ) {
            Text("Sign In", fontSize = 34.sp)
            Spacer(modifier = Modifier.height(100.dp))
            LabeledInputField(
                label = "Email",
                placeholder = "example@gmail.com",
                value = loginState.email,
                isError = loginState.emailError != null,
                onValueChange = { loginViewModel.onLoginEvent(LoginFormEvent.EmailChanged(it)) })
            Spacer(Modifier.height(25.dp))
            LabeledInputField(
                label = "Password",
                placeholder = "password...",
                value = loginState.password,
                onValueChange = { loginViewModel.onLoginEvent(LoginFormEvent.PasswordChanged(it)) },
                isPassword = true,
                isError = loginState.passwordError != null
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    loginViewModel.onLoginEvent(LoginFormEvent.OnSubmit(onSuccess = {
                        navController.navigate(NavigationItem.Main.route) {
                            popUpTo(NavigationItem.Auth.route) {
                                inclusive = true
                            }
                        }
                    }, onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            "Sign In",
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            Text(
                text = "Don't have an account?",
                color = Color.Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable {
                        navController.navigate(NavigationItem.Register.route)
                    }
            )
        }
    }
}
