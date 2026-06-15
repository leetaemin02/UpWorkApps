package com.example.jobsearchapp.model

data class User(
    val id: Int? = null,
    val hoTen: String,
    val email: String,
    val soDienThoai: String,
    val matKhau: String,
    val vaiTro: String        // "Ứng viên" hoặc "Nhà tuyển dụng"
)