package com.example.watwallet.feature.auth.ui

import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.watwallet.core.navigation.NavigationItem
import com.example.watwallet.feature.auth.viewmodel.AuthViewModel
import com.example.watwallet.ui.components.LabeledInputField
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(navController: NavController){

    val viewModel: AuthViewModel = koinViewModel()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 100.dp, horizontal = 20.dp)) {
            Text("Sign In", fontSize = 34.sp)
            Spacer(modifier = Modifier.height(100.dp))
            LabeledInputField("Email", "example@gmail.com", email, onValueChange = {email = it})
            Spacer(Modifier.height(25.dp))
            LabeledInputField("Password", "password...", password, onValueChange = {password = it}, isPassword = true)
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    viewModel.login(email, password) {
                        navController.navigate(NavigationItem.Main.route) {
                            popUpTo(NavigationItem.Auth.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Sign In", textAlign = TextAlign.Center, color = Color.White, fontSize = 24.sp)
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
