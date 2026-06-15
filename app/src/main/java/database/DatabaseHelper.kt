package com.example.jobsearchapp.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "JobSearch.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Bảng User (Tài khoản)
        val createTableUser = """
            CREATE TABLE User (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                email TEXT,
                matKhau TEXT,
                vaiTro TEXT
            )
        """.trimIndent()

        // 2. Bảng Job (Công việc)
        val createTableJob = """
            CREATE TABLE Job (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tieuDe TEXT,
                tenCongTy TEXT,
                moTa TEXT,
                luong REAL,
                diaDiem TEXT
            )
        """.trimIndent()

        // 3. Bảng Applicant (Hồ sơ ứng viên ứng tuyển)
        val createTableApplicant = """
            CREATE TABLE Applicant (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jobId INTEGER,
                tenUngVien TEXT,
                emailUngVien TEXT,
                sdtUngVien TEXT,
                trangThai TEXT,
                FOREIGN KEY(jobId) REFERENCES Job(id)
            )
        """.trimIndent()

        db.execSQL(createTableUser)
        db.execSQL(createTableJob)
        db.execSQL(createTableApplicant)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS User")
        db.execSQL("DROP TABLE IF EXISTS Job")
        db.execSQL("DROP TABLE IF EXISTS Applicant")
        onCreate(db)
    }
}