package com.example.watwallet.data.repository

import com.example.watwallet.utils.DateUtils
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.local.ReferenceSet
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.sql.Date
import java.sql.Time

data class CreateJobModel(
    val description: String,
    val employerUid: String,
    val location: GeoPoint,
    val locationInfo: String,
    val position: String,
)

data class JobGetModel(
    val id: String,
    val employer:Employer,
    val description: String,
    val location: GeoPoint,
    val locationInfo: String,
    val position: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class JobUpdateModel(
    val id: String,
    val employer:Employer,
    val description: String,
    val location: GeoPoint,
    val locationInfo: String,
    val position: String,
    val startDate: Timestamp,
    val endDate: Timestamp
)

class JobRepositoryImpl(private val userRepository: UserRepository) : JobRepository {

    private var db: FirebaseFirestore = Firebase.firestore
    private var auth: FirebaseAuth = Firebase.auth

    private suspend fun addJobToUser(newCreatedJob: DocumentReference, startDate:LocalDate, endDate: LocalDate){

        val currentSeason = db.collection("seasons").whereEqualTo("current",true).get().await().first()

        val jobInfoMap = hashMapOf(
            "startDate" to Timestamp(startDate.atStartOfDayIn(TimeZone.UTC).epochSeconds, 0),
            "endDate" to Timestamp(endDate.atStartOfDayIn(TimeZone.UTC).epochSeconds, 0),
            "jobId" to newCreatedJob
        )

        val jobSeasonMap = hashMapOf(
            "job" to jobInfoMap,
            "season" to currentSeason.reference,
            "deleted" to false
        )

        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User is not authenticated.")

        // Add the jobSeasonMap to the user's seasonJobs array
        db.collection("users")
            .document(currentUser.uid)
            .update("seasonJobs", FieldValue.arrayUnion(jobSeasonMap))
            .await()
    }

    override suspend fun createJob(job: CreateJobModel, startDate: LocalDate, endDate: LocalDate) {

        val employerRef = db.collection("employers").document(job.employerUid)

        val jobMap = hashMapOf(
            "active" to true,
            "description" to job.description,
            "locationInfo" to job.locationInfo,
            "position" to job.position,
            "employer" to employerRef,
            "location" to job.location
        )

        val newJobResult = db.collection("jobs").add(jobMap).await()

        addJobToUser(newJobResult, startDate, endDate)
    }

    override suspend fun getJob(jobId:String):JobGetModel?{
        val job = db.collection("jobs").document(jobId).get().await()
        val employer = job.getDocumentReference("employer")?.get()?.await()


        return if(job != null){

            val userJob = userRepository.getUser()!!.userInfo.seasonJobs.first { it.job.job.uid == job.id }

            JobGetModel(
                id = job.id,
                description = job.getString("description") ?: "",
                locationInfo = job.getString("locationInfo") ?: "",
                location = job.getGeoPoint("location") ?: GeoPoint(0.0,0.0),
                position = job.getString("position") ?: "",
                startDate = DateUtils.timestampToLocalDate(userJob.job.startDate),
                endDate = DateUtils.timestampToLocalDate(userJob.job.endDate),
                employer = Employer(
                    uid = employer?.id ?: "",
                    name = employer?.getString("name") ?: ""
                )
            )
        }else{
            null
        }

    }

    override suspend fun updateJob(job: JobUpdateModel){
        val employerRef = db.collection("employers").document(job.employer.uid)
        val jobUpdateData = hashMapOf(
            "location" to job.location,
            "locationInfo" to job.locationInfo,
            "description" to job.description,
            "position" to job.position,
            "employer" to employerRef,
            "active" to true
        )
        db.collection("jobs").document(job.id).update(jobUpdateData).await()
        var userJobs = userRepository.getUser()!!.userInfo.seasonJobs
        userJobs = userJobs.map { seasonJob ->
            if (seasonJob.job.job.uid == job.id) {
                val updatedJob = seasonJob.job.copy(
                    startDate = job.startDate,
                    endDate = job.endDate
                )
                seasonJob.copy(job = updatedJob)
            } else {
                seasonJob
            }
        }

        userRepository.updateSeasonJobs(userJobs)

    }

}