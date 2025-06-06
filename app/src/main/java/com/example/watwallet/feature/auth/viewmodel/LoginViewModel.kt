package com.example.watwallet.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.feature.addtransaction.viewmodel.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class LoginUISate(
    var email: String = "",
    var password: String = "",
    var emailError: String? = null,
    var passwordError: String? = null
)

sealed class LoginFormEvent(
) {
    data class EmailChanged(val email: String) : LoginFormEvent()
    data class PasswordChanged(val password: String) : LoginFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit, val onError: (String) -> Unit) : LoginFormEvent()
}

class LoginFormValidator {
    fun validateEmail(email: String?): ValidationResult {
        return if (email.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Email cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = "Success"
            )
        }
    }

    fun validatePassword(password: String?): ValidationResult {
        return if (password.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Password cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = "Success"
            )
        }
    }
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val loginValidator: LoginFormValidator = LoginFormValidator()
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUISate())
    val loginState: StateFlow<LoginUISate> = _loginState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun onLoginEvent(event: LoginFormEvent) {
        when (event) {
            is LoginFormEvent.EmailChanged -> {
                _loginState.update { it.copy(email = event.email) }
                if (loginValidator.validateEmail(event.email).isSuccess && _loginState.value.emailError != null) {
                    _loginState.update { it.copy(emailError = null) }
                }
            }

            is LoginFormEvent.OnSubmit -> {
                login(onSuccess = { event.onSuccess() }, onError = { message -> event.onError(message) })
            }

            is LoginFormEvent.PasswordChanged -> {
                _loginState.update { it.copy(password = event.password) }
                if (loginValidator.validatePassword(event.password).isSuccess && _loginState.value.passwordError != null) {
                    _loginState.update { it.copy(passwordError = null) }
                }
            }
        }
    }

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.update { true }

            var hasError = false

            val emailValidationResult = loginValidator.validateEmail(_loginState.value.email)
            if (!emailValidationResult.isSuccess) {
                _loginState.update { it.copy(emailError = emailValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            val passwordValidationResult =
                loginValidator.validatePassword(_loginState.value.password)
            if (!passwordValidationResult.isSuccess) {
                _loginState.update { it.copy(passwordError = passwordValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            if (hasError) return@launch

            val loginResponse =
                authRepository.login(_loginState.value.email, _loginState.value.password)
            if (loginResponse.isSuccess) {
                onSuccess()
            } else {
                onError(loginResponse.errorMessage!!)
            }
            _loading.update { false }
        }
    }
}