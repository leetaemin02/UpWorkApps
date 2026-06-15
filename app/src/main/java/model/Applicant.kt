package com.example.jobsearchapp.model

data class Applicant(
    val id: Int? = null,
    val jobId: Int,
    val tenUngVien: String,
    val emailUngVien: String,
    val sdtUngVien: String,
    val trangThai: String // "Chờ duyệt", "Chấp nhận", "Từ chối"
)