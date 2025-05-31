package com.example.watwallet.data.repository

import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.type.DateTime
import kotlinx.coroutines.tasks.await

data class User(
    var uid: String,
    var email: String,
    var accessToken: String,
    var userInfo: UserInfo,
)

data class UserInfo(
    val id: String,
    val name: String,
    val surname: String,
    val phone: String
)

class UserRepositoryImpl : UserRepository {

    private var auth: FirebaseAuth = Firebase.auth
    private var db: FirebaseFirestore = Firebase.firestore

    private var cachedUser: User? = null

    override suspend fun getUser(): User? {
        return try {
            cachedUser ?: loadUserData()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun loadUserData(): User? {
        return try {
            val userAuth = auth.currentUser ?: return null
            val accessToken = auth.currentUser?.getIdToken(true)?.await()?.token ?: return null
            val userDoc = db.collection("users").document(userAuth.uid).get().await()

            if (!userDoc.exists()) return null

            // Parse basic user fields
            val name = userDoc.getString("name") ?: ""
            val surname = userDoc.getString("surname") ?: ""
            val phone = userDoc.getString("phone") ?: ""

            val userInfo = UserInfo(
                id = userAuth.uid,
                name = name,
                surname = surname,
                phone = phone
            )

            val userState = User(
                uid = userAuth.uid,
                email = userAuth.email!!,
                accessToken = accessToken,
                userInfo = userInfo
            )

            cachedUser = userState
            userState
        } catch (e: Exception) {
            null
        }
    }



    override suspend fun updateUserInfo(): User? {
        return loadUserData()
    }

    override fun clearUserCache() {
        cachedUser = null
    }
}
