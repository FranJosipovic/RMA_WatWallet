package com.example.watwallet.data.repository

interface JobRepository {
    suspend fun createJob(jobCreateModel: JobCreateModel);
    suspend fun getJob(userId: String, jobId: String): JobGetModel?
    suspend fun updateJob(jobId: String, job: JobUpdateModel)
    suspend fun getJobs(userId: String): List<JobGetModel>
    suspend fun deleteJob(userId: String, jobId: String)
}