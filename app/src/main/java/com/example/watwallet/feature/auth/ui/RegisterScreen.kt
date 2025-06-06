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
import com.example.watwallet.feature.auth.viewmodel.RegisterFormEvent
import com.example.watwallet.feature.auth.viewmodel.RegisterViewModel
import com.example.watwallet.ui.components.LabeledInputField
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(navController: NavController) {

    val registerViewModel: RegisterViewModel = koinViewModel()
    val state by registerViewModel.registerState.collectAsState()

    val loading by registerViewModel.loading.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = 20.dp)
        ) {
            Text("Sign Up", fontSize = 34.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LabeledInputField(
                label = "Email",
                placeholder = "example@gmail.com",
                value = state.email,
                onValueChange = { registerViewModel.onRegisterEvent(RegisterFormEvent.EmailChanged(it)) },
                isError = state.emailError != null
            )
            Spacer(Modifier.height(10.dp))
            LabeledInputField(
                label = "Password",
                placeholder = "password...",
                value = state.password,
                onValueChange = { registerViewModel.onRegisterEvent(RegisterFormEvent.PasswordChanged(it)) },
                isPassword = true,
                isError = state.passwordError != null
            )
            Spacer(Modifier.height(10.dp))
            LabeledInputField(
                label = "Name",
                placeholder = "name",
                value = state.name,
                onValueChange = { registerViewModel.onRegisterEvent(RegisterFormEvent.NameChanged(it)) },
                isError = state.nameError != null
            )
            Spacer(Modifier.height(10.dp))
            LabeledInputField(
                label = "Surname",
                placeholder = "surname...",
                value = state.surname,
                onValueChange = { registerViewModel.onRegisterEvent(RegisterFormEvent.SurnameChanged(it)) },
                isError = state.surnameError != null
            )
            Spacer(Modifier.height(10.dp))
            LabeledInputField(
                label = "Phone",
                placeholder = "+385...",
                value = state.phone,
                onValueChange = { registerViewModel.onRegisterEvent(RegisterFormEvent.PhoneChanged(it)) },
                isError = state.phoneError != null
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    registerViewModel.onRegisterEvent(RegisterFormEvent.OnSubmit(onSuccess = {
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
                            text = "Sign Up",
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            Text(
                text = "Already" +
                        " have an account?",
                color = Color.Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate(NavigationItem.Login.route)
                }
            )
        }
    }
}