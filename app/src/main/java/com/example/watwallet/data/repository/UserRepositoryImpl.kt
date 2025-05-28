package com.example.watwallet.data.repository

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.sql.Time

//collection: employers
data class Employer(
    val uid: String,
    val name: String
)

//collection: jobs
data class Job(
    val uid: String,
    val employer: Employer, //reference in firebase(collection/uid)
    val description: String,
    val location: GeoPoint,
    val locationInfo: String,
    val position: String
)

data class User(
    var uid:String,
    var email: String,
    var accessToken: String,
    var userInfo: UserInfo,
)

//collection: seasons
data class Season(
    val id: String,
    val current: Boolean,
    val season: Number
)

data class SeasonJob(
    val season: Season, //reference in firebase
    val job: JobUser,
    val deleted:Boolean
)

data class JobUser(
    val startDate: Timestamp,
    val endDate: Timestamp,
    val job: Job,
)

//collection: users
data class UserInfo(
    val id: String, //connection to firebase authentication user uid
    val name:String,
    val surname: String,
    val phone: String,
    val seasonJobs: List<SeasonJob>
)

class UserRepositoryImpl : UserRepository {

    private var auth: FirebaseAuth = Firebase.auth
    private var db: FirebaseFirestore = Firebase.firestore

    private var cachedUser: User? = null

    override suspend fun getUser(): User? {
        return try {
            cachedUser ?: loadUserData()
        }catch(e:Exception) {
            null
        }
    }

    override suspend fun loadUserData():User?{
        return try {
            val userAuth = auth.currentUser ?: return null
            val accessToken = auth.currentUser?.getIdToken(true)?.await()?.token ?: return null
            val userDoc = db.collection("users").document(userAuth.uid).get().await()

            if (!userDoc.exists()) return null

            // Parse basic user fields
            val name = userDoc.getString("name") ?: ""
            val surname = userDoc.getString("surname") ?: ""
            val phone = userDoc.getString("phone") ?: ""

            // Parse seasonJobs
            val seasonJobsRefs =
                (userDoc.get("seasonJobs") as? List<Map<String, Any>>) ?: emptyList()

            val seasonJobs = seasonJobsRefs.map { sj ->
                val seasonRef = sj["season"] as? DocumentReference
                val jobUserMap = sj["job"] as? Map<String, Any>

                val seasonDoc = seasonRef?.get()?.await()
                val jobUser = parseJobUser(jobUserMap)

                SeasonJob(
                    season = Season(
                        id = seasonDoc!!.id,
                        season = seasonDoc.get("season") as Number,
                        current = seasonDoc.getBoolean("current")!!
                    ),
                    job = jobUser,
                    deleted = sj["deleted"] as Boolean
                )
            }.filter {
                !it.deleted
            }

            val userInfo = UserInfo(
                id = userAuth.uid,
                name = name,
                surname = surname,
                phone = phone,
                seasonJobs = seasonJobs
            )

            val userState = User(
                uid = userAuth.uid,
                email = userAuth.email!!,
                accessToken = accessToken,
                userInfo = userInfo
            )

            cachedUser = userState
            userState
        }catch (e:Exception){
            null
        }
    }

    private suspend fun parseJobUser(map: Map<String, Any>?): JobUser {
        val startDate = map?.get("startDate") as? Timestamp ?: Timestamp.now()
        val endDate = map?.get("endDate") as? Timestamp ?: Timestamp.now()

        val jobRef = map?.get("jobId") as? DocumentReference
        val jobDoc = jobRef?.get()?.await()
        val jobMap = jobDoc?.data ?: emptyMap()

        val employerRef = jobMap["employer"] as? DocumentReference
        val employerDoc = employerRef?.get()?.await()

        val job = Job(
            uid = jobDoc?.id ?: "",
            employer = Employer(
                uid = employerRef?.id ?: "",
                name = employerDoc?.getString("name") ?: ""
            ),
            description = jobMap["description"] as? String ?: "",
            location = jobMap["location"] as? GeoPoint ?: GeoPoint(0.0, 0.0),
            locationInfo = jobMap["locationInfo"] as? String ?: "",
            position = jobMap["position"] as? String ?: ""
        )

        // Returning JobUser with start and end dates as LocalDate
        return JobUser(
            startDate = startDate,
            endDate = endDate,
            job = job
        )
    }

    override suspend fun updateUserInfo(): User? {
        return loadUserData()
    }

    override fun clearUserCache() {
        cachedUser = null
    }

    override suspend fun softDeleteJob(uid: String) {
        try {
            // Get the current user UID
            val userUid = Firebase.auth.currentUser?.uid
                ?: throw IllegalStateException("No authenticated user found")

            // Build the job reference for matching
            val jobDocumentRef = db.collection("jobs").document(uid)

            // Find the user document
            val userRef = db.collection("users").document(userUid)

            // Get the current seasonJobs list
            val seasonJobsRef = userRef.get().await().get("seasonJobs") as List<Map<String, Any>>

            // Update the matching job to set deleted = true
            val updatedSeasonJobs = seasonJobsRef.map { sj ->
                val job = sj["job"] as? Map<String, Any>
                val matches = job!!["jobId"] as DocumentReference == jobDocumentRef

                if (matches) {
                    sj.toMutableMap().apply {
                        this["deleted"] = true
                    }
                } else {
                    sj
                }
            }

            userRef.update("seasonJobs", updatedSeasonJobs).await()

            println("Successfully soft deleted job: $uid")

        } catch (e: Exception) {
            println("Error soft deleting job: ${e.message}")
            throw e
        }
    }

    override suspend fun updateSeasonJobs(seasonJobs:List<SeasonJob>){

        val updatedSeasonJobsHashMap = seasonJobs.map {
            val seasonRef = db.collection("seasons").document(it.season.id)
            val jobRef = db.collection("jobs").document(it.job.job.uid)
            val job = hashMapOf(
                "endDate" to it.job.endDate,
                "startDate" to it.job.startDate,
                "jobId" to jobRef
            )
            hashMapOf(
                "deleted" to false,
                "season" to seasonRef,
                "job" to job
            )
        }
        db.collection("users").document(cachedUser!!.uid).update("seasonJobs", updatedSeasonJobsHashMap).await()
        cachedUser = cachedUser?.copy(userInfo = cachedUser?.userInfo?.copy(seasonJobs = seasonJobs)!!)
    }
}
