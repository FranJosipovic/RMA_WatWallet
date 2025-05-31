package com.example.watwallet.data.repository

import android.util.Log
import com.example.watwallet.utils.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate

data class JobCreateModel(
    val description: String,
    val employerId: String,
    val locationInfo: String,
    val locationLongitude: Double,
    val locationLatitude: Double,
    val position: String,
    val season: Number,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class JobUpdateModel(
    val description: String,
    val employerId: String,
    val userId: String,
    val locationInfo: String,
    val location: GeoPoint,
    val position: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class JobGetModel(
    val id: String,
    val description: String,
    val employer: EmployerGetModel,
    val locationInfo: String,
    val locationLatitude: Double,
    val locationLongitude: Double,
    val position: String,
    val season: Number,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class Job(
    val active: Boolean = false,
    val deleted: Boolean = false,
    val description: String = "",
    val employer: DocumentReference? = null,
    val locationInfo: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val position: String = "",
    val season: Long = DateUtils.currentYear.toLong(),
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp = Timestamp.now()
)

class JobRepositoryImpl : JobRepository {

    private var db: FirebaseFirestore = Firebase.firestore

    override suspend fun createJob(jobCreateModel: JobCreateModel) {

        val employerRef = db.collection("employers").document(jobCreateModel.employerId)

        val job = Job(
            active = true,
            deleted = false,
            description = jobCreateModel.description,
            employer = employerRef,
            locationInfo = jobCreateModel.locationInfo,
            location = GeoPoint(jobCreateModel.locationLatitude, jobCreateModel.locationLongitude),
            position = jobCreateModel.position,
            season = jobCreateModel.season.toLong(),
            startDate = DateUtils.localDateToTimestamp(jobCreateModel.startDate),
            endDate = DateUtils.localDateToTimestamp(jobCreateModel.endDate)
        )

        try {
            db.collection("users").document(jobCreateModel.userId).collection("jobs").add(job)
                .await()
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to add job", e)
        }
    }

    override suspend fun getJobs(userId: String): List<JobGetModel> {
        val jobsRes = db.collection("users").document(userId).collection("jobs")
            .whereEqualTo("deleted", false).get().await()
        return jobsRes.documents.mapNotNull {
            mapDocumentSnapshotToJobGetModel(it)
        }
    }

    override suspend fun deleteJob(userId: String, jobId: String) {
        db.collection("users").document(userId).collection("jobs").document(jobId)
            .update("deleted", true).await()
    }

    override suspend fun getJob(userId: String, jobId: String): JobGetModel? {
        val jobRes =
            db.collection("users").document(userId).collection("jobs").document(jobId).get()
                .await()
        return mapDocumentSnapshotToJobGetModel(jobRes);
    }

    private suspend fun mapDocumentSnapshotToJobGetModel(jobSnapshot: DocumentSnapshot): JobGetModel? {
        return try {
            val job = jobSnapshot.toObject<Job>()

            val employerRes = jobSnapshot.getDocumentReference("employer")?.get()?.await()
            val employer = employerRes?.toObject<Employer>()

            if (job == null || employer == null) return null

            JobGetModel(
                id = jobSnapshot.id,
                description = job.description,
                employer = EmployerGetModel(
                    id = employerRes.id,
                    name = employer.name
                ),
                locationInfo = job.locationInfo,
                locationLatitude = job.location.latitude,
                locationLongitude = job.location.longitude,
                position = job.position,
                season = job.season,
                startDate = DateUtils.timestampToLocalDate(job.startDate),
                endDate = DateUtils.timestampToLocalDate(job.endDate)
            )
        } catch (e: Exception) {
            e.message?.let { Log.e("Exc", it) }
            null
        }

    }

    override suspend fun updateJob(jobId: String, job: JobUpdateModel) {

        val employerRef = db.collection("employers").document(job.employerId)

        val jobMap = hashMapOf(
            "description" to job.description,
            "employer" to employerRef,
            "locationInfo" to job.locationInfo,
            "location" to job.location,
            "position" to job.position,
            "startDate" to DateUtils.localDateToTimestamp(job.startDate),
            "endDate" to DateUtils.localDateToTimestamp(job.endDate)
        )
        db.collection("users").document(job.userId).collection("jobs").document(jobId)
            .update(jobMap).await()
    }
}