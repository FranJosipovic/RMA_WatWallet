package com.example.watwallet.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

data class CreateJobDTO(
    val description: String,
    val employer: DocumentReference,
    val locationInfo: String,
    val location: GeoPoint,
    val position: String,
    val season: DocumentReference,
    val startDate: Timestamp,
    val endDate: Timestamp
)

data class JobUpdateModel(
    val description: String,
    val employer: DocumentReference,
    val locationInfo: String,
    val location: GeoPoint,
    val position: String,
    val startDate: Timestamp,
    val endDate: Timestamp
)

data class Job(
    val id: String,
    val active: Boolean,
    val deleted: Boolean,
    val description: String,
    val employer: DocumentReference,
    val locationInfo: String,
    val location: GeoPoint,
    val position: String,
    val season: DocumentReference,
    val startDate: Timestamp,
    val endDate: Timestamp
)

class JobRepositoryImpl : JobRepository {

    private var db: FirebaseFirestore = Firebase.firestore
    private var auth: FirebaseAuth = Firebase.auth

    override suspend fun createJob(userId: String, job: CreateJobDTO) {
        val jobMap = hashMapOf(
            "active" to true,
            "deleted" to false,
            "description" to job.description,
            "employer" to job.employer,
            "locationInfo" to job.locationInfo,
            "location" to job.location,
            "position" to job.position,
            "season" to job.season,
            "startDate" to job.startDate,
            "endDate" to job.endDate
        )

        try {
            db.collection("users").document(userId).collection("jobs").add(jobMap).await()
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to add job", e)
        }
    }

    override suspend fun getJobs(userId: String): List<Job> {
        val jobsRes = db.collection("users").document(userId).collection("jobs").get().await()
        return jobsRes.documents.mapNotNull {
            Job(
                id = it.id,
                active = it.getBoolean("active") ?: true,
                deleted = it.getBoolean("deleted") ?: false,
                description = it.getString("description") ?: "",
                employer = it.getDocumentReference("employer")!!,
                locationInfo = it.getString("locationInfo") ?: "",
                location = it.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                position = it.getString("position") ?: "",
                season = it.getDocumentReference("season")!!,
                startDate = it.getTimestamp("startDate") ?: Timestamp.now(),
                endDate = it.getTimestamp("endDate") ?: Timestamp.now()
            )
        }
    }

    override suspend fun deleteJob(userId: String, jobId: String) {
        db.collection("users").document(userId).collection("jobs").document(jobId)
            .update("deleted", true).await()
    }

    override suspend fun getJob(jobId: String): Job {
        val job =
            db.collection("users").document(auth.uid!!).collection("jobs").document(jobId).get()
                .await()

        return Job(
            id = job.id,
            active = job.getBoolean("active") ?: true,
            deleted = job.getBoolean("deleted") ?: false,
            description = job.getString("description") ?: "",
            employer = job.getDocumentReference("employer")!!,
            locationInfo = job.getString("locationInfo") ?: "",
            location = job.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
            position = job.getString("position") ?: "",
            season = job.getDocumentReference("seasonId")!!,
            startDate = job.getTimestamp("startDate") ?: Timestamp.now(),
            endDate = job.getTimestamp("endDate") ?: Timestamp.now()
        )
    }

    override suspend fun updateJob(userId: String, jobId: String, job: JobUpdateModel) {
        val jobMap = hashMapOf(
            "description" to job.description,
            "employer" to job.employer,
            "locationInfo" to job.locationInfo,
            "location" to job.location,
            "position" to job.position,
            "startDate" to job.startDate,
            "endDate" to job.endDate
        )
        db.collection("users").document(userId).collection("jobs").document(jobId)
            .update(jobMap).await()
    }
}