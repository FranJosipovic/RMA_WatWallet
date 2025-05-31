package com.example.watwallet.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class EmployerRepositoryImpl : EmployerRepository {

    private val db = Firebase.firestore

    override suspend fun search(searchTerm: String): List<EmployerGetModel> {
        return try {
            val result = db.collection("employers")
                .get()
                .await()

            val lowerSearch = searchTerm.lowercase()

            result.documents.mapNotNull { doc ->
                val uid = doc.id
                val name = doc.getString("name") ?: return@mapNotNull null
                EmployerGetModel(uid, name)
            }.filter { employer ->
                employer.name.lowercase().contains(lowerSearch)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun create(employerCreateModel: EmployerCreateModel): EmployerGetModel? {
        return try {
            val res = db.collection("employers").add(employerCreateModel).await()
            val doc = res.get().await()
            EmployerGetModel(
                id = doc.id,
                name = doc.getString("name") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun get(count: Long?): List<EmployerGetModel> {
        val query = db.collection("employers")

        val snapshot = if (count != null) {
            query.limit(count).get().await()
        } else {
            query.get().await()
        }

        return snapshot.documents.mapNotNull { EmployerGetModel(it.id, it.getString("name") ?: "") }
    }

    override suspend fun get(employerId: String): EmployerGetModel? {
        val employerRes = db.collection("employers").document(employerId).get().await()
        val employer = employerRes.toObject<Employer>() ?: return null
        return EmployerGetModel(
            id = employerRes.id,
            name = employer.name
        )
    }
}