package com.example.watwallet.data.repository

import com.example.watwallet.data.model.login.LoginResponse
import com.example.watwallet.data.model.login.RegisterResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {
    private var auth: FirebaseAuth = Firebase.auth
    private var db: FirebaseFirestore = Firebase.firestore

    override suspend fun isAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun login(email: String, password: String): LoginResponse {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()

            val user = result.user
            if (user != null) {
                LoginResponse(isSuccess = true, errorMessage = null)
            } else {
                LoginResponse(isSuccess = false, errorMessage = "User authentication failed.")
            }

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            LoginResponse(isSuccess = false, errorMessage = "Invalid email or password.")

        } catch (e: FirebaseAuthInvalidUserException) {
            LoginResponse(isSuccess = false, errorMessage = "No account found with this email.")

        } catch (e: FirebaseAuthException) {
            LoginResponse(isSuccess = false, errorMessage = "Authentication error: ${e.message}")

        } catch (e: Exception) {
            LoginResponse(isSuccess = false, errorMessage = "Unexpected error: ${e.message}")
        }
    }

    override suspend fun register(user: RegisterUser): RegisterResponse {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val firebaseUser =
                authResult.user ?: return RegisterResponse(false, "User creation failed.")

            // Get UID
            val uid = firebaseUser.uid

            val userInfo = mapOf(
                "uid" to uid,
                "name" to user.name,
                "surname" to user.surname,
                "phone" to user.phone,
                "seasonJobs" to emptyList<Any>()
            )

            db.collection("users").document(uid).set(userInfo).await()

            // Return successful response
            RegisterResponse(isSuccess = true, errorMessage = null)

        } catch (e: FirebaseAuthWeakPasswordException) {
            RegisterResponse(false, "Weak password. Please choose a stronger password.")

        } catch (e: FirebaseAuthUserCollisionException) {
            RegisterResponse(false, "An account already exists with this email.")

        } catch (e: FirebaseAuthInvalidCredentialsException) {
            RegisterResponse(false, "Invalid email format.")

        } catch (e: FirebaseFirestoreException) {
            RegisterResponse(false, "Failed to save user data. Please try again.")

        } catch (e: Exception) {
            RegisterResponse(false, "Unexpected error: ${e.localizedMessage}")
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
