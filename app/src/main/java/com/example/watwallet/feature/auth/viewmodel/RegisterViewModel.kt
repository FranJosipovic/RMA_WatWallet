package com.example.watwallet.feature.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watwallet.data.repository.AuthRepository
import com.example.watwallet.data.repository.RegisterUser
import com.example.watwallet.feature.addtransaction.viewmodel.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUISate(
    var email: String = "",
    var password: String = "",
    var name: String = "",
    var surname: String = "",
    var phone: String = "",
    var emailError: String? = null,
    var passwordError: String? = null,
    var nameError: String? = null,
    var surnameError: String? = null,
    var phoneError: String? = null
)

sealed class RegisterFormEvent(
) {
    data class EmailChanged(val email: String) : RegisterFormEvent()
    data class PasswordChanged(val password: String) : RegisterFormEvent()
    data class NameChanged(val name: String) : RegisterFormEvent()
    data class SurnameChanged(val surname: String) : RegisterFormEvent()
    data class PhoneChanged(val phone: String) : RegisterFormEvent()
    data class OnSubmit(val onSuccess: () -> Unit, val onError: (String) -> Unit) :
        RegisterFormEvent()
}

class RegisterFormValidator {
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

    fun validateName(name: String?): ValidationResult {
        return if (name.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Name cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = "Success"
            )
        }
    }

    fun validateSurname(surname: String?): ValidationResult {
        return if (surname.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Surname cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = "Success"
            )
        }
    }

    fun validatePhone(phone: String?): ValidationResult {
        return if (phone.isNullOrEmpty()) {
            ValidationResult(
                isSuccess = false,
                message = "Phone cannot be empty"
            )
        } else {
            ValidationResult(
                isSuccess = true,
                message = "Success"
            )
        }
    }
}

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val registerValidator: RegisterFormValidator = RegisterFormValidator()
) : ViewModel() {

    private val _registerState = MutableStateFlow(RegisterUISate())
    val registerState: StateFlow<RegisterUISate> = _registerState.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun onRegisterEvent(event: RegisterFormEvent) {
        when (event) {
            is RegisterFormEvent.EmailChanged -> {
                _registerState.update { it.copy(email = event.email) }
                if (registerValidator.validateEmail(event.email).isSuccess && _registerState.value.emailError != null) {
                    _registerState.update { it.copy(emailError = null) }
                }
            }

            is RegisterFormEvent.NameChanged -> {
                _registerState.update { it.copy(name = event.name) }
                if (registerValidator.validateName(event.name).isSuccess && _registerState.value.nameError != null) {
                    _registerState.update { it.copy(nameError = null) }
                }
            }

            is RegisterFormEvent.OnSubmit -> {
                register(onSuccess = { event.onSuccess() }, onError = { event.onError(it) })
            }

            is RegisterFormEvent.PasswordChanged -> {
                _registerState.update { it.copy(password = event.password) }
                if (registerValidator.validatePassword(event.password).isSuccess && _registerState.value.passwordError != null) {
                    _registerState.update { it.copy(passwordError = null) }
                }
            }

            is RegisterFormEvent.PhoneChanged -> {
                _registerState.update { it.copy(phone = event.phone) }
                if (registerValidator.validatePhone(event.phone).isSuccess && _registerState.value.phoneError != null) {
                    _registerState.update { it.copy(phoneError = null) }
                }
            }

            is RegisterFormEvent.SurnameChanged -> {
                _registerState.update { it.copy(surname = event.surname) }
                if (registerValidator.validateSurname(event.surname).isSuccess && _registerState.value.surnameError != null) {
                    _registerState.update { it.copy(surnameError = null) }
                }
            }
        }
    }

    private fun register(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _loading.update { true }
            var hasError = false
            val emailValidationResult = registerValidator.validateEmail(_registerState.value.email)
            if (!emailValidationResult.isSuccess) {
                _registerState.update { it.copy(emailError = emailValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            val passwordValidationResult =
                registerValidator.validatePassword(_registerState.value.password)
            if (!passwordValidationResult.isSuccess) {
                _registerState.update { it.copy(passwordError = passwordValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            val phoneValidationResult =
                registerValidator.validatePhone(_registerState.value.phone)
            if (!phoneValidationResult.isSuccess) {
                _registerState.update { it.copy(phoneError = phoneValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            val nameValidationResult = registerValidator.validateName(_registerState.value.name)
            if (!nameValidationResult.isSuccess) {
                _registerState.update { it.copy(nameError = nameValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            val surnameValidationResult =
                registerValidator.validateSurname(_registerState.value.surname)
            if (!surnameValidationResult.isSuccess) {
                _registerState.update { it.copy(surnameError = surnameValidationResult.message) }
                _loading.update { false }
                hasError = true
            }

            if (hasError) return@launch

            val loginResponse = authRepository.register(
                RegisterUser(
                    email = _registerState.value.email,
                    password = _registerState.value.password,
                    name = _registerState.value.name,
                    surname = _registerState.value.surname,
                    phone = _registerState.value.phone
                )
            )
            if (loginResponse.isSuccess) {
                onSuccess()
            } else {
                onError(loginResponse.errorMessage ?: "Error registering user")
            }
            _loading.update { false }
        }
    }
}
