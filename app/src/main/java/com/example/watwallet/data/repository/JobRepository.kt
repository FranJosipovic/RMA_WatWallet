package com.example.watwallet.data.repository

interface JobRepository {
    suspend fun createJob(userId: String, job: CreateJobDTO);
    suspend fun getJob(jobId: String): Job
    suspend fun updateJob(userId: String, jobId: String, job: JobUpdateModel)
    suspend fun getJobs(userId: String): List<Job>
    suspend fun deleteJob(userId: String, jobId: String)
}