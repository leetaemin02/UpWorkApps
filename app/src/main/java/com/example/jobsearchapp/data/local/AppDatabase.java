package com.example.jobsearchapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.jobsearchapp.data.models.*;

@Database(entities = {
        User.class, 
        Job.class, 
        Application.class, 
        SavedJob.class, 
        Candidate.class, 
        Company.class,
        Notification.class,
        Category.class,
        Review.class
}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract JobDao jobDao();
    public abstract ApplicationDao applicationDao();
    public abstract SavedJobDao savedJobDao();
    public abstract CandidateDao candidateDao();
    public abstract CompanyDao companyDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "jobsearch_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
            
            // Xóa sạch dữ liệu cũ (chạy một lần nếu bạn muốn dọn dẹp)
            instance.clearAllTables();
            
            // Bỏ hoặc comment dòng này để không tự thêm dữ liệu mẫu nữa
            // instance.seedData();
        }
        return instance;
    }

    private void seedData() {
        if (jobDao().getAllJobs().isEmpty()) {
            Job job1 = new Job();
            job1.setTitle("Senior UX Designer");
            job1.setSalaryMin(3000);
            job1.setSalaryMax(4000);
            job1.setLocation("Hồ Chí Minh");
            job1.setCompanyName("Google");
            job1.setJobType("Toàn thời gian");
            job1.setCategory("Thiết kế");
            jobDao().insertJob(job1);

            Job job2 = new Job();
            job2.setTitle("Product Manager");
            job2.setSalaryMin(2500);
            job2.setSalaryMax(3500);
            job2.setLocation("Hà Nội");
            job2.setCompanyName("Spotify");
            job2.setJobType("Toàn thời gian");
            job2.setCategory("Kinh doanh");
            jobDao().insertJob(job2);
        }
    }
}
