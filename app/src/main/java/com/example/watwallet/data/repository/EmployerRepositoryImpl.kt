package com.example.watwallet.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class EmployerRepositoryImpl : EmployerRepository{

    private val db = Firebase.firestore

    override suspend fun search(searchTerm: String): List<Employer> {
        return try {
            val result = db.collection("employers")
                .get()
                .await()

            val lowerSearch = searchTerm.lowercase()

            result.documents.mapNotNull { doc ->
                val uid = doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                Employer(uid, name)
            }.filter { employer ->
                employer.name.lowercase().contains(lowerSearch)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun create(employerName: String): Employer? {
        return try {
            val employer = hashMapOf("name" to employerName)
            val res = db.collection("employers").add(employer).await()
            val doc = res.get().await()
            Employer(
                uid = doc.id,
                name = doc.getString("name") ?: ""
            )
        }catch (e:Exception){
            null
        }

    }

}