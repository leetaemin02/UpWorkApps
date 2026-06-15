package com.example.jobsearchapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "CareerLaunch.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Bảng User
        val createTableUser = """
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                hoTen TEXT,
                email TEXT UNIQUE,
                soDienThoai TEXT,
                matKhau TEXT,
                vaiTro TEXT -- "Ứng viên" hoặc "Nhà tuyển dụng"
            )
        """.trimIndent()

        // 2. Bảng Job
        val createTableJob = """
            CREATE TABLE Job (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tieuDe TEXT,
                tenCongTy TEXT,
                nganhNghe TEXT,
                diaDiem TEXT,
                luong TEXT, -- Lưu dạng chuỗi ví dụ: "25 - 40 Triệu"
                kinhNghiem TEXT, -- Ví dụ: "3 - 5 Năm"
                hinhThuc TEXT, -- Ví dụ: "Toàn thời gian"
                hanNop TEXT, -- Ví dụ: "30/11/2023"
                moTa TEXT,
                yeuCau TEXT,
                quyenLoi TEXT,
                ngayDang TEXT
            )
        """.trimIndent()

        // 3. Bảng Applicant / Candidate
        val createTableApplicant = """
            CREATE TABLE Applicant (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jobId INTEGER,
                userId INTEGER, -- Người nộp đơn
                ngayNop TEXT,
                trangThai TEXT, -- "Mới nộp", "Đang xem xét", "Phỏng vấn", "Từ chối"
                FOREIGN KEY(jobId) REFERENCES Job(id),
                FOREIGN KEY(userId) REFERENCES User(id)
            )
        """.trimIndent()

        // 4. Bảng Profile / CV (Khớp màn Hồ sơ cá nhân)
        val createTableProfile = """
            CREATE TABLE Profile (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userId INTEGER,
                tieuDeHoSo TEXT, -- Ví dụ: "Chuyên viên Phát triển Phần mềm Cao cấp"
                kyNang TEXT, -- Lưu chuỗi cách nhau bởi dấu phẩy: "React.js, Node.js, Python"
                kinhNghiemLamViec TEXT, -- Đoạn văn mô tả các công ty cũ đã làm
                duongDanCV TEXT, -- Lưu đường dẫn file PDF trong máy của user
                FOREIGN KEY(userId) REFERENCES User(id)
            )
        """.trimIndent()


        db.execSQL(createTableUser)
        db.execSQL(createTableJob)
        db.execSQL(createTableApplicant)
        db.execSQL(createTableProfile)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Job")
        db.execSQL("DROP TABLE IF EXISTS Applicant")
        db.execSQL("DROP TABLE IF EXISTS Profile")
        onCreate(db)
    }
}