package com.example.jobsearchapp.model

data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val matKhau: String,
    val vaiTro: String // "NhaTuyenDung" hoặc "NguoiTimViec"
)