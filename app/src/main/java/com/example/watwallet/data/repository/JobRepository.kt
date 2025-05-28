package com.example.watwallet.data.repository

import kotlinx.datetime.LocalDate

interface JobRepository {
    suspend fun createJob(job:CreateJobModel, startDate: LocalDate, endDate: LocalDate);
    suspend fun getJob(jobId:String):JobGetModel?
    suspend fun updateJob(job: JobUpdateModel)
}