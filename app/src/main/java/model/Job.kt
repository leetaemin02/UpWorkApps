package com.example.jobsearchapp.model

data class Job(
    val id: Int? = null,
    val tieuDe: String,       // Tên công việc
    val tenCongTy: String,     // Tên công ty
    val moTa: String,          // Mô tả công việc
    val luong: Double,         // Mức lương
    val diaDiem: String        // Địa điểm
)